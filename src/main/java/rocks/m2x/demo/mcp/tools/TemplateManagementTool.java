package rocks.m2x.demo.mcp.tools;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import rocks.m2x.demo.mcp.model.TemplateMetadata;
import rocks.m2x.demo.service.mcp.TemplateResourceService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP tool for template management
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TemplateManagementTool {
    private final TemplateResourceService templateResourceService;

    @McpTool(
            name = "provide_template",
            description = "Provide template content for report generation. " +
                    "Creates a temporary template resource that can be used in generate_report calls. " +
                    "Templates are automatically cleaned up after a configurable TTL (default: 1 hour)."
    )
    public ProvideTemplateResult provideTemplate(
            @McpToolParam(description = "Template identifier/name for this session", required = true)
            String templateName,
            @McpToolParam(description = "Base64 encoded .docx file content", required = true)
            String templateContent,
            @McpToolParam(description = "Template description/purpose/structure", required = false)
            String description) {
        try {
            TemplateMetadata metadata = templateResourceService.createTemplateFromBase64(
                    templateName,
                    templateContent,
                    description != null ? description : "Template provided via MCP");

            return ProvideTemplateResult.builder()
                    .success(true)
                    .templateId(metadata.getId())
                    .templateUri("template://" + metadata.getId())
                    .message("Template created successfully")
                    .metadata(Map.of(
                            "name", metadata.getName() != null ? metadata.getName() : "",
                            "description", metadata.getDescription() != null ? metadata.getDescription() : "",
                            "version", metadata.getVersion() != null ? metadata.getVersion() : "",
                            "uploaded", metadata.getUploaded() != null ? metadata.getUploaded().toString() : ""
                    ))
                    .build();

        } catch (SecurityException e) {
            log.warn("Security violation in provide_template: {}", e.getMessage());
            return ProvideTemplateResult.builder()
                    .success(false)
                    .error("Invalid template name")
                    .message("Template name contains invalid characters")
                    .build();
        } catch (Exception e) {
            log.error("Failed to provide template", e);
            return ProvideTemplateResult.builder()
                    .success(false)
                    .error("Template creation failed")
                    .message("Failed to create template. Please check template content and try again.")
                    .build();
        }
    }

    @McpTool(
            name = "list_templates",
            description = "Get list of available templates and their metadata. " +
                    "Returns temporary templates created via provide_template. " +
                    "Note: This lists all templates across all sessions/clients. " +
                    "Templates are automatically cleaned up after TTL expiration. " +
                    "For production use, prefer using the template ID returned from provide_template directly."
    )
    public ListTemplatesResult listTemplates() {
        try {
            Map<String, TemplateMetadata> templates = templateResourceService.listTemplates();

            List<TemplateInfo> templateInfos = templates.values().stream()
                    .map(metadata -> TemplateInfo.builder()
                            .id(metadata.getId())
                            .name(metadata.getName())
                            .description(metadata.getDescription())
                            .version(metadata.getVersion())
                            .uploaded(metadata.getUploaded() != null ? metadata.getUploaded().toString() : null)
                            .build())
                    .collect(Collectors.toList());

            return ListTemplatesResult.builder()
                    .success(true)
                    .templates(templateInfos)
                    .count(templateInfos.size())
                    .build();

        } catch (Exception e) {
            log.error("Failed to list templates", e);
            return ListTemplatesResult.builder()
                    .success(false)
                    .error("Template listing failed")
                    .message("Failed to list templates. Please try again later.")
                    .build();
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class ProvideTemplateResult {
        boolean success;
        String templateId;
        String templateUri; // URI for referencing this template (e.g., template://my-template)
        String message;
        String error;
        Map<String, String> metadata;
    }

    @lombok.Data
    @lombok.Builder
    public static class ListTemplatesResult {
        boolean success;
        List<TemplateInfo> templates;
        int count;
        String error;
        String message;
    }

    @lombok.Data
    @lombok.Builder
    public static class TemplateInfo {
        String id;
        String name;
        String description;
        String version;
        String uploaded;
    }
}
