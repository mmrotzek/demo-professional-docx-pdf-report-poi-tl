package rocks.m2x.demo.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import rocks.m2x.demo.mcp.model.McpReportRequest;
import rocks.m2x.demo.mcp.model.ReportOptions;
import rocks.m2x.demo.service.mcp.McpReportService;

import java.util.Map;

/**
 * MCP tool for generating reports
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReportGenerationTool {
    private final McpReportService mcpReportService;
    private final ObjectMapper objectMapper;

    @McpTool(
            name = "generate_report",
            description = "Generate DOCX/PDF reports from structured data and templates. " +
                    "Supports both predefined templates and inline templates provided as base64 encoded .docx files."
    )
    public GenerateReportResult generateReport(
            @McpToolParam(description = "Template identifier (predefined template name, filesystem path, or resource URI from provide_template). " +
                    "If templateContent is provided, this is optional and used as the template name.", required = false)
            String templateId,
            @McpToolParam(description = "Base64 encoded .docx template content (optional, used for inline templates). " +
                    "If provided, a temporary template will be created.", required = false)
            String templateContent,
            @McpToolParam(description = "Report data as JSON object matching the template placeholders. " +
                    "The structure should match the placeholders in the template (e.g., {{customerName}}, {{items}}).", required = true)
            Map<String, Object> reportData,
            @McpToolParam(description = "Output format: 'docx' or 'pdf' (default: 'docx')", required = false)
            String outputFormat,
            @McpToolParam(description = "Report options as JSON object. Supports: " +
                    "readonly (boolean, default: true), " +
                    "draft (boolean, default: false), " +
                    "enforceUpdateFields (boolean, default: false), " +
                    "htmlFields (comma-separated list of field names that should be rendered as HTML)", required = false)
            Map<String, Object> options) throws Exception {
        try {
            McpReportRequest request = McpReportRequest.builder()
                    .templateId(templateId)
                    .templateContent(templateContent)
                    .reportData(reportData)
                    .outputFormat(outputFormat != null && !outputFormat.isEmpty() ? outputFormat : "docx")
                    .options(parseOptions(options))
                    .build();

            String base64Document = mcpReportService.generateReport(request);

            return GenerateReportResult.builder()
                    .success(true)
                    .document(base64Document)
                    .format(outputFormat != null && !outputFormat.isEmpty() ? outputFormat : "docx")
                    .message("Report generated successfully")
                    .build();

        } catch (Exception e) {
            log.error("Failed to generate report", e);
            return GenerateReportResult.builder()
                    .success(false)
                    .error(e.getMessage())
                    .message("Failed to generate report: " + e.getMessage())
                    .build();
        }
    }

    private ReportOptions parseOptions(Map<String, Object> options) {
        if (options == null || options.isEmpty()) {
            return ReportOptions.builder().build();
        }

        ReportOptions.ReportOptionsBuilder builder = ReportOptions.builder();

        if (options.containsKey("readonly")) {
            builder.readonly(Boolean.parseBoolean(options.get("readonly").toString()));
        }
        if (options.containsKey("draft")) {
            builder.draft(Boolean.parseBoolean(options.get("draft").toString()));
        }
        if (options.containsKey("enforceUpdateFields")) {
            builder.enforceUpdateFields(Boolean.parseBoolean(options.get("enforceUpdateFields").toString()));
        }
        if (options.containsKey("htmlFields")) {
            builder.htmlFields(options.get("htmlFields").toString());
        }

        return builder.build();
    }

    @lombok.Data
    @lombok.Builder
    public static class GenerateReportResult {
        boolean success;
        String document; // Base64 encoded document
        String format; // docx or pdf
        String message;
        String error;
    }
}
