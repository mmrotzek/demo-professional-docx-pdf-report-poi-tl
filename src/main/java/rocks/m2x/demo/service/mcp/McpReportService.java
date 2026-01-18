package rocks.m2x.demo.service.mcp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import rocks.m2x.demo.mcp.model.McpReportRequest;
import rocks.m2x.demo.mcp.model.ReportOptions;
import rocks.m2x.demo.service.pdf.DocxToPdfService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

/**
 * Main MCP service for report generation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class McpReportService {
    private final TemplateResourceService templateResourceService;
    private final GenericReportService genericReportService;
    private final DocxToPdfService docxToPdfService;
    private final DataValidationService validationService;

    /**
     * Generate a report from a request
     *
     * @param request MCP report request
     * @return Base64 encoded document (DOCX or PDF)
     * @throws IOException if generation fails
     */
    public String generateReport(McpReportRequest request) throws IOException {
        Resource templateResource;

        // Get template resource
        if (request.getTemplateContent() != null && !request.getTemplateContent().isEmpty()) {
            // Create temporary template from base64 content
            String templateId = request.getTemplateId() != null ? request.getTemplateId() : "inline-template";
            templateResourceService.createTemplateFromBase64(
                    templateId,
                    request.getTemplateContent(),
                    "Inline template provided in request");
            templateResource = templateResourceService.getTemplateResource(templateId);
        } else if (request.getTemplateId() != null) {
            // Use existing template
            templateResource = templateResourceService.getTemplateResource(request.getTemplateId());
        } else {
            throw new IllegalArgumentException("Either templateId or templateContent must be provided");
        }

        // Get report options
        ReportOptions options = request.getOptions() != null ? request.getOptions() : ReportOptions.builder().build();

        // Render DOCX report
        ByteArrayOutputStream docxOutput = genericReportService.renderReport(
                templateResource,
                request.getReportData(),
                options);

        byte[] docxBytes = docxOutput.toByteArray();

        // Convert to PDF if requested
        if ("pdf".equalsIgnoreCase(request.getOutputFormat())) {
            try {
                ByteArrayOutputStream pdfOutput = docxToPdfService.convertDocxToPdf(docxBytes);
                return Base64.getEncoder().encodeToString(pdfOutput.toByteArray());
            } catch (rocks.m2x.demo.service.exc.InvalidConfigurationException | rocks.m2x.demo.service.exc.PdfConversionException e) {
                throw new IOException("PDF conversion failed", e);
            }
        } else {
            return Base64.getEncoder().encodeToString(docxBytes);
        }
    }

    /**
     * Validate report data
     *
     * @param templateId Template identifier
     * @param reportData Report data to validate
     * @return Validation result
     */
    public rocks.m2x.demo.mcp.model.ValidationResult validateData(String templateId, Map<String, Object> reportData) {
        var metadata = templateResourceService.getTemplateMetadata(templateId);
        if (metadata != null && metadata.getDataSchema() != null) {
            return validationService.validate(reportData, metadata.getDataSchema());
        } else {
            // Basic validation without schema
            return validationService.validateBasic(reportData, metadata != null ? metadata.getRequiredFields() : null);
        }
    }
}
