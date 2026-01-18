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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    /**
     * Create a temporary template resource from base64 content
     *
     * @param templateName Template identifier
     * @param base64Content Base64 encoded .docx file content
     * @param description Template description
     * @return TemplateMetadata with resource URI
     * @throws IOException if template creation fails
     */
    public TemplateMetadata createTemplateFromBase64(String templateName, String base64Content, String description)
            throws IOException {
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

        // Create temporary file
        Path tempDir = Paths.get(mcpProperties.getTempStorageDir(), "mcp-templates");
        Files.createDirectories(tempDir);
        String templateId = templateName != null ? templateName : UUID.randomUUID().toString();
        File tempFile = tempDir.resolve(templateId + ".docx").toFile();

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(templateBytes);
        }

        // Create metadata
        TemplateMetadata metadata = TemplateMetadata.builder()
                .id(templateId)
                .name(templateName != null ? templateName : templateId)
                .description(description)
                .version("1.0")
                .uploaded(LocalDateTime.now())
                .build();

        templates.put(templateId, metadata);
        templateTimestamps.put(templateId, LocalDateTime.now());

        log.info("Created temporary template: {} at {}", templateId, tempFile.getAbsolutePath());
        return metadata;
    }

    /**
     * Get template resource by ID
     *
     * @param templateId Template identifier
     * @return Resource for the template
     * @throws IllegalArgumentException if template not found
     */
    public Resource getTemplateResource(String templateId) {
        TemplateMetadata metadata = templates.get(templateId);
        if (metadata == null) {
            // Check if it's a predefined template or filesystem path
            return resolveTemplateSource(templateId);
        }

        // Clean up expired templates
        cleanupExpiredTemplates();

        Path tempDir = Paths.get(mcpProperties.getTempStorageDir(), "mcp-templates");
        File templateFile = tempDir.resolve(templateId + ".docx").toFile();

        if (!templateFile.exists()) {
            throw new IllegalArgumentException("Template file not found: " + templateId);
        }

        return new FileSystemResource(templateFile);
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
     * Resolve template source (predefined template, filesystem path, or classpath)
     *
     * @param templateSource Template identifier or path
     * @return Resource for the template
     */
    private Resource resolveTemplateSource(String templateSource) {
        // Prevent path traversal attacks
        if (templateSource.contains("..") || templateSource.startsWith("/")) {
            throw new SecurityException("Invalid template path: " + templateSource);
        }

        // Try as filesystem path
        File file = new File(templateSource);
        if (file.exists() && file.isFile()) {
            return new FileSystemResource(file);
        }

        // Try as classpath resource
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
                    Path tempDir = Paths.get(mcpProperties.getTempStorageDir(), "mcp-templates");
                    File templateFile = tempDir.resolve(templateId + ".docx").toFile();
                    if (templateFile.exists()) {
                        Files.delete(templateFile.toPath());
                        log.info("Deleted expired template: {}", templateId);
                    }
                } catch (IOException e) {
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
     * @param templateId Template identifier
     */
    public void deleteTemplate(String templateId) {
        templates.remove(templateId);
        templateTimestamps.remove(templateId);
        try {
            Path tempDir = Paths.get(mcpProperties.getTempStorageDir(), "mcp-templates");
            File templateFile = tempDir.resolve(templateId + ".docx").toFile();
            if (templateFile.exists()) {
                Files.delete(templateFile.toPath());
                log.info("Deleted template: {}", templateId);
            }
        } catch (IOException e) {
            log.warn("Failed to delete template file: {}", templateId, e);
        }
    }
}
