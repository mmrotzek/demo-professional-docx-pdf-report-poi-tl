package rocks.m2x.demo.mcp.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Request model for MCP report generation
 */
@Data
@Builder
public class McpReportRequest {
    /**
     * Template identifier (predefined template name, filesystem path, or resource URI from provide_template)
     */
    String templateId;

    /**
     * Base64 encoded .docx template content (optional, used for inline templates)
     */
    String templateContent;

    /**
     * Report data as JSON structure matching the template placeholders
     */
    Map<String, Object> reportData;

    /**
     * Output format: "docx" or "pdf"
     */
    @Builder.Default
    String outputFormat = "docx";

    /**
     * Report generation options
     */
    @Builder.Default
    ReportOptions options = ReportOptions.builder().build();
}
