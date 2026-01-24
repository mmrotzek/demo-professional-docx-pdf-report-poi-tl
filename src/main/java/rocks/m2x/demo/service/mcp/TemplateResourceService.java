package rocks.m2x.demo.service.mcp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import rocks.m2x.demo.config.McpProperties;
import rocks.m2x.demo.mcp.model.TemplateMetadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Service for managing template resources (temporary and persistent)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateResourceService {
    private final McpProperties mcpProperties;
    private final Map<String, TemplateMetadata> templates = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> templateTimestamps = new ConcurrentHashMap<>();
    
    // Pattern to detect path separators (Unix /, Windows \, and other separators)
    private static final Pattern PATH_SEPARATOR_PATTERN = Pattern.compile("[/\\\\]");
    
    // Allowlist of known classpath template names to prevent arbitrary resource loading
    private static final Set<String> ALLOWED_CLASSPATH_TEMPLATES = Set.of(
            "template_soa.docx",
            "template_soa_table.docx",
            "template_invoice.docx"
    );

    /**
     * Validate template name to prevent path traversal attacks
     *
     * @param templateName Template name to validate
     * @throws SecurityException if template name contains path separators or other disallowed characters
     */
    private void validateTemplateName(String templateName) {
        if (templateName == null) {
            return;
        }
        
        // Reject names containing path separators
        if (PATH_SEPARATOR_PATTERN.matcher(templateName).find()) {
            throw new SecurityException("Template name contains path separators: " + templateName);
        }
        
        // Reject names containing path traversal sequences
        if (templateName.contains("..")) {
            throw new SecurityException("Template name contains path traversal sequence: " + templateName);
        }
        
        // Reject absolute paths (Unix and Windows)
        if (templateName.startsWith("/") || templateName.matches("^[A-Za-z]:[/\\\\].*")) {
            throw new SecurityException("Template name cannot be an absolute path: " + templateName);
        }
    }
    
    /**
     * Get secure template file path with canonical path validation
     *
     * @param templateId Server-generated template ID (UUID)
     * @return Secure File path within the template directory
     * @throws SecurityException if path traversal is detected
     */
    private File getSecureTemplateFile(String templateId) throws IOException {
        Path tempDir = Paths.get(mcpProperties.getTempStorageDir(), "mcp-templates");
        Files.createDirectories(tempDir);
        
        // Use only the templateId (which should be a UUID) as filename
        // Normalize the path to resolve any potential issues
        Path templatePath = tempDir.resolve(templateId + ".docx").normalize();
        
        // Verify the resolved path is still within the base directory
        Path baseDir = tempDir.toAbsolutePath().normalize();
        Path resolvedPath = templatePath.toAbsolutePath().normalize();
        
        if (!resolvedPath.startsWith(baseDir)) {
            throw new SecurityException("Path traversal detected: template ID " + templateId + " resolves outside template directory");
        }
        
        return resolvedPath.toFile();
    }

    /**
     * Create a temporary template resource from base64 content
     *
     * @param templateName Template name (for metadata only, not used as ID)
     * @param base64Content Base64 encoded .docx file content
     * @param description Template description
     * @return TemplateMetadata with resource URI
     * @throws IOException if template creation fails
     * @throws SecurityException if template name contains invalid characters
     */
    public TemplateMetadata createTemplateFromBase64(String templateName, String base64Content, String description)
            throws IOException {
        // Validate template name if provided (reject path separators)
        if (templateName != null) {
            validateTemplateName(templateName);
        }
        
        // Validate size
        long decodedSize = (base64Content.length() * 3L) / 4;
        if (decodedSize > mcpProperties.getMaxTemplateSize()) {
            throw new IllegalArgumentException("Template size exceeds maximum allowed size: " + mcpProperties.getMaxTemplateSize());
        }

        // Decode base64 content
        byte[] templateBytes = Base64.getDecoder().decode(base64Content);

        // Validate that it's a valid .docx file (basic check)
        if (mcpProperties.isValidateTemplateContent()) {
            validateDocxContent(templateBytes);
        }

        // Always generate server-side UUID for template ID (never use user input)
        String templateId = UUID.randomUUID().toString();
        
        // Get secure file path with canonical path validation
        File tempFile = getSecureTemplateFile(templateId);

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(templateBytes);
        }

        // Create metadata with server-generated ID and user-provided name (stored separately)
        TemplateMetadata metadata = TemplateMetadata.builder()
                .id(templateId)
                .name(templateName != null ? templateName : templateId)
                .description(description)
                .version("1.0")
                .uploaded(LocalDateTime.now())
                .build();

        templates.put(templateId, metadata);
        templateTimestamps.put(templateId, LocalDateTime.now());

        log.info("Created temporary template: {} (name: {})", templateId, templateName != null ? templateName : "unnamed");
        return metadata;
    }

    /**
     * Get template resource by ID
     *
     * @param templateId Template identifier (server-generated UUID)
     * @return Resource for the template
     * @throws IllegalArgumentException if template not found
     * @throws SecurityException if path traversal is detected
     */
    public Resource getTemplateResource(String templateId) {
        TemplateMetadata metadata = templates.get(templateId);
        if (metadata == null) {
            // Check if it's a predefined classpath template
            return resolveTemplateSource(templateId);
        }

        // Clean up expired templates
        cleanupExpiredTemplates();

        try {
            File templateFile = getSecureTemplateFile(templateId);
            if (!templateFile.exists()) {
                throw new IllegalArgumentException("Template file not found: " + templateId);
            }
            return new FileSystemResource(templateFile);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to resolve template file: " + templateId, e);
        }
    }

    /**
     * Get template metadata
     *
     * @param templateId Template identifier
     * @return TemplateMetadata or null if not found
     */
    public TemplateMetadata getTemplateMetadata(String templateId) {
        cleanupExpiredTemplates();
        return templates.get(templateId);
    }

    /**
     * List all templates
     *
     * @return Map of template IDs to metadata
     */
    public Map<String, TemplateMetadata> listTemplates() {
        cleanupExpiredTemplates();
        return Map.copyOf(templates);
    }

    /**
     * Resolve template source (classpath templates only)
     *
     * @param templateSource Template identifier (classpath template name)
     * @return Resource for the template
     * @throws SecurityException if path traversal is detected or template not in allowlist
     */
    private Resource resolveTemplateSource(String templateSource) {
        // Prevent path traversal attacks - reject any path separators or traversal sequences
        if (templateSource == null || templateSource.isEmpty()) {
            throw new SecurityException("Template source cannot be null or empty");
        }
        
        if (PATH_SEPARATOR_PATTERN.matcher(templateSource).find() || 
            templateSource.contains("..") || 
            templateSource.startsWith("/")) {
            throw new SecurityException("Invalid template path: " + templateSource);
        }

        // Enforce allowlist for classpath templates to prevent arbitrary resource loading
        if (!ALLOWED_CLASSPATH_TEMPLATES.contains(templateSource)) {
            throw new SecurityException("Template not in allowlist: " + templateSource);
        }

        // Only allow classpath templates - no filesystem path support
        try {
            Resource classpathResource = new org.springframework.core.io.DefaultResourceLoader()
                    .getResource("classpath:templates/" + templateSource);
            if (classpathResource.exists()) {
                return classpathResource;
            }
        } catch (Exception e) {
            log.debug("Template not found in classpath: {}", templateSource);
        }

        throw new IllegalArgumentException("Template not found: " + templateSource);
    }

    /**
     * Validate that bytes represent a valid .docx file
     *
     * @param bytes File bytes
     * @throws IllegalArgumentException if not a valid .docx file
     */
    private void validateDocxContent(byte[] bytes) {
        if (bytes.length < 4) {
            throw new IllegalArgumentException("File too small to be a valid .docx file");
        }

        // Check ZIP file signature (DOCX files are ZIP archives)
        // ZIP files start with: 50 4B 03 04 (PK..)
        if (bytes[0] != 0x50 || bytes[1] != 0x4B || bytes[2] != 0x03 || bytes[3] != 0x04) {
            throw new IllegalArgumentException("File does not appear to be a valid .docx file (missing ZIP signature)");
        }
    }

    /**
     * Clean up expired temporary templates
     */
    private void cleanupExpiredTemplates() {
        LocalDateTime expirationTime = LocalDateTime.now().minus(mcpProperties.getTemplateTtl());
        templateTimestamps.entrySet().removeIf(entry -> {
            if (entry.getValue().isBefore(expirationTime)) {
                String templateId = entry.getKey();
                templates.remove(templateId);
                try {
                    File templateFile = getSecureTemplateFile(templateId);
                    if (templateFile.exists()) {
                        Files.delete(templateFile.toPath());
                        log.info("Deleted expired template: {}", templateId);
                    }
                } catch (IOException | SecurityException e) {
                    log.warn("Failed to delete expired template file: {}", templateId, e);
                }
                return true;
            }
            return false;
        });
    }

    /**
     * Delete a template
     *
     * @param templateId Template identifier (server-generated UUID)
     * @throws SecurityException if path traversal is detected
     */
    public void deleteTemplate(String templateId) {
        templates.remove(templateId);
        templateTimestamps.remove(templateId);
        try {
            File templateFile = getSecureTemplateFile(templateId);
            if (templateFile.exists()) {
                Files.delete(templateFile.toPath());
                log.info("Deleted template: {}", templateId);
            }
        } catch (IOException | SecurityException e) {
            log.warn("Failed to delete template file: {}", templateId, e);
        }
    }
}
