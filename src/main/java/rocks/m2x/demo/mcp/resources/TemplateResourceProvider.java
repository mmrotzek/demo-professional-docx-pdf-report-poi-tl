package rocks.m2x.demo.mcp.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;
import rocks.m2x.demo.mcp.model.TemplateMetadata;
import rocks.m2x.demo.service.mcp.TemplateResourceService;

/**
 * MCP resource provider for template schemas and metadata
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TemplateResourceProvider {
    private final TemplateResourceService templateResourceService;
    private final ObjectMapper objectMapper;

    @McpResource(
            uri = "template://schema/{templateId}",
            name = "Template Schema",
            description = "JSON Schema defining expected data structure for a template"
    )
    public String getTemplateSchema(String templateId) {
        try {
            TemplateMetadata metadata = templateResourceService.getTemplateMetadata(templateId);
            if (metadata != null && metadata.getDataSchema() != null) {
                return objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(metadata.getDataSchema());
            } else {
                return "{\"message\":\"No schema defined for template: " + templateId + "\"}";
            }
        } catch (SecurityException e) {
            log.warn("Security violation accessing template schema: {}", e.getMessage());
            return "{\"error\":\"Template access denied\"}";
        } catch (Exception e) {
            log.error("Failed to get template schema", e);
            return "{\"error\":\"Failed to retrieve template schema\"}";
        }
    }

    @McpResource(
            uri = "template://info/{templateId}",
            name = "Template Information",
            description = "Template metadata including name, description, version, and capabilities"
    )
    public String getTemplateInfo(String templateId) {
        try {
            TemplateMetadata metadata = templateResourceService.getTemplateMetadata(templateId);
            if (metadata != null) {
                StringBuilder info = new StringBuilder();
                info.append("Template ID: ").append(metadata.getId()).append("\n");
                info.append("Name: ").append(metadata.getName() != null ? metadata.getName() : "N/A").append("\n");
                info.append("Description: ").append(metadata.getDescription() != null ? metadata.getDescription() : "N/A").append("\n");
                info.append("Version: ").append(metadata.getVersion() != null ? metadata.getVersion() : "N/A").append("\n");
                if (metadata.getUploaded() != null) {
                    info.append("Uploaded: ").append(metadata.getUploaded()).append("\n");
                }
                if (metadata.getRequiredFields() != null && !metadata.getRequiredFields().isEmpty()) {
                    info.append("Required Fields: ").append(String.join(", ", metadata.getRequiredFields())).append("\n");
                }
                if (metadata.getHtmlFields() != null && !metadata.getHtmlFields().isEmpty()) {
                    info.append("HTML Fields: ").append(String.join(", ", metadata.getHtmlFields())).append("\n");
                }
                return info.toString();
            } else {
                return "Template not found: " + templateId;
            }
        } catch (SecurityException e) {
            log.warn("Security violation accessing template info: {}", e.getMessage());
            return "Error: Template access denied";
        } catch (Exception e) {
            log.error("Failed to get template info", e);
            return "Error: Failed to retrieve template information";
        }
    }
}
