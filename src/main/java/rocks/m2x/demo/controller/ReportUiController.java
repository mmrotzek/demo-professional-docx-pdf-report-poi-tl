package rocks.m2x.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import rocks.m2x.demo.Constants;
import rocks.m2x.demo.config.PdfConversionHealthIndicator;
import rocks.m2x.demo.mcp.model.McpReportRequest;
import rocks.m2x.demo.mcp.model.ReportOptions;
import rocks.m2x.demo.service.mcp.McpReportService;
import rocks.m2x.demo.service.mcp.TemplateResourceService;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Simple HTML UI for generating reports using existing MCP services.
 */
@Controller
@RequestMapping("/ui/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportUiController {

    private final TemplateResourceService templateResourceService;
    private final McpReportService mcpReportService;
    private final ObjectMapper objectMapper;
    private final PdfConversionHealthIndicator pdfConversionHealthIndicator;

    private static final List<String> BUILTIN_TEMPLATES = List.of(
            "template_soa.docx",
            "template_soa_table.docx",
            "template_invoice.docx"
    );

    private static final String SAMPLE_JSON_RESOURCE = "data/soa-report.json";
    private static final String SAMPLE_JSON_INVOICE_RESOURCE = "data/complex-invoice.json";

    @GetMapping
    public String showForm(Model model) {
        populateCommonModel(model);
        // Defaults
        if (!model.containsAttribute("outputFormat")) {
            model.addAttribute("outputFormat", Constants.DEFAULT_FORMAT);
        }
        if (!model.containsAttribute("download")) {
            model.addAttribute("download", Boolean.FALSE);
        }

        return "report-ui";
    }

    @GetMapping("/sample-json")
    public ResponseEntity<String> sampleJson() {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(getSampleJson());
    }

    @GetMapping("/sample-json-invoice")
    public ResponseEntity<String> sampleJsonInvoice() {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(getSampleJsonInvoice());
    }

    @GetMapping("/templates")
    public ResponseEntity<Map<String, Object>> getTemplates() {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("builtinTemplates", BUILTIN_TEMPLATES);
        response.put("uploadedTemplates", templateResourceService.listTemplates());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @PostMapping("/upload-template")
    public ResponseEntity<?> uploadTemplate(@RequestParam("templateFile") MultipartFile templateFile,
                                             @RequestParam(value = "templateName", required = false) String templateName,
                                             @RequestParam(value = "description", required = false) String description) {
        if (templateFile == null || templateFile.isEmpty()) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Template file is required.");
        }

        if (!templateFile.getOriginalFilename().toLowerCase().endsWith(".docx")) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Only .docx files are supported.");
        }

        try {
            byte[] bytes = templateFile.getBytes();
            String base64Content = Base64.getEncoder().encodeToString(bytes);
            
            String finalTemplateName = StringUtils.hasText(templateName) 
                    ? templateName 
                    : (StringUtils.hasText(templateFile.getOriginalFilename()) 
                            ? templateFile.getOriginalFilename().replace(".docx", "") 
                            : "uploaded-template");
            
            rocks.m2x.demo.mcp.model.TemplateMetadata metadata = templateResourceService.createTemplateFromBase64(
                    finalTemplateName,
                    base64Content,
                    StringUtils.hasText(description) ? description : "Template uploaded via UI");

            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("templateId", metadata.getId());
            response.put("templateName", metadata.getName());
            response.put("message", "Template uploaded successfully");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid template file uploaded", e);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Invalid template file: " + e.getMessage());
        } catch (SecurityException e) {
            log.warn("Security violation in template upload", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Invalid template name: " + e.getMessage());
        } catch (IOException e) {
            log.error("I/O error while uploading template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Error uploading template: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while uploading template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Unexpected error while uploading template.");
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateReport(@RequestParam(value = "templateFile", required = false) MultipartFile templateFile,
                                            @RequestParam(value = "templateId", required = false) String templateId,
                                            @RequestParam("reportDataJson") String reportDataJson,
                                            @RequestParam(value = "outputFormat", defaultValue = Constants.DEFAULT_FORMAT) String outputFormat,
                                            @RequestParam(value = "download", defaultValue = "false") boolean download,
                                            @RequestParam(value = "htmlFields", required = false) String htmlFields) {

        // Basic validation
        if ((templateFile == null || templateFile.isEmpty()) &&
                !StringUtils.hasText(templateId)) {
            return plainBadRequest("Please select a template or upload a DOCX file.");
        }

        if (!StringUtils.hasText(reportDataJson)) {
            return plainBadRequest("Report data JSON is required.");
        }

        outputFormat = outputFormat.toLowerCase();
        if (!Constants.FILE_EXT_DOCX.equalsIgnoreCase(outputFormat)
                && !Constants.FILE_EXT_PDF.equalsIgnoreCase(outputFormat)) {
            return plainBadRequest("Output format must be 'docx' or 'pdf'.");
        }

        Map<String, Object> reportData;
        try {
            reportData = objectMapper.readValue(reportDataJson, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            log.warn("Invalid JSON in report UI", e);
            return plainBadRequest("Invalid JSON: " + e.getOriginalMessage());
        }

        try {
            String templateIdForRequest;
            String templateContentBase64 = null;

            if (templateFile != null && !templateFile.isEmpty()) {
                byte[] bytes = templateFile.getBytes();
                templateContentBase64 = Base64.getEncoder().encodeToString(bytes);
                templateIdForRequest = StringUtils.hasText(templateFile.getOriginalFilename())
                        ? templateFile.getOriginalFilename()
                        : "uploaded-template.docx";
            } else {
                templateIdForRequest = templateId;
            }

            // Build report options
            ReportOptions.ReportOptionsBuilder optionsBuilder = ReportOptions.builder();
            if (StringUtils.hasText(htmlFields)) {
                optionsBuilder.htmlFields(htmlFields.trim());
            }

            McpReportRequest request = McpReportRequest.builder()
                    .templateId(templateIdForRequest)
                    .templateContent(templateContentBase64)
                    .reportData(reportData)
                    .outputFormat(outputFormat)
                    .options(optionsBuilder.build())
                    .build();

            String base64Result = mcpReportService.generateReport(request);
            byte[] fileBytes = Base64.getDecoder().decode(base64Result.getBytes(StandardCharsets.UTF_8));

            String fileExt = Constants.FILE_EXT_PDF.equalsIgnoreCase(outputFormat)
                    ? Constants.FILE_EXT_PDF
                    : Constants.FILE_EXT_DOCX;
            String contentType = Constants.FILE_EXT_PDF.equalsIgnoreCase(outputFormat)
                    ? Constants.CONTENT_TYPE_PDF
                    : Constants.CONTENT_TYPE_DOCX;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(fileBytes.length);

            String dispositionType = download ? "attachment" : "inline";
            String fileName = "report." + fileExt;
            headers.set(HttpHeaders.CONTENT_DISPOSITION, dispositionType + "; filename=" + fileName);

            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error while generating report from UI", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("Error generating report: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("I/O error while generating report from UI", e);
            // Check if it's a PDF conversion error
            Throwable cause = e.getCause();
            String errorMessage = "I/O error while generating report.";
            if (cause != null && cause.getMessage() != null && cause.getMessage().contains("PDF conversion service is not available")) {
                errorMessage = cause.getMessage();
            } else if (e.getMessage() != null && e.getMessage().contains("PDF conversion failed")) {
                // Extract the underlying cause message if available
                if (cause != null && cause.getMessage() != null) {
                    errorMessage = cause.getMessage();
                } else {
                    errorMessage = e.getMessage();
                }
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(errorMessage.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Unexpected error while generating report from UI", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Unexpected error while generating report.".getBytes(StandardCharsets.UTF_8));
        }
    }

    private ResponseEntity<byte[]> plainBadRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.TEXT_PLAIN)
                .body(message.getBytes(StandardCharsets.UTF_8));
    }

    private void populateCommonModel(Model model) {
        model.addAttribute("builtinTemplates", BUILTIN_TEMPLATES);
        model.addAttribute("uploadedTemplates", templateResourceService.listTemplates());

        // Check PDF conversion service health
        Health pdfHealth = pdfConversionHealthIndicator.health();
        model.addAttribute("pdfServiceHealth", pdfHealth.getStatus().getCode());
        model.addAttribute("pdfServiceHealthDetails", pdfHealth.getDetails());
    }

    private String getSampleJson() {
        try {
            return new ClassPathResource(SAMPLE_JSON_RESOURCE).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Missing sample SoA data: " + SAMPLE_JSON_RESOURCE, e);
        }
    }
    private String getSampleJsonInvoice() {
        try {
            return new ClassPathResource(SAMPLE_JSON_INVOICE_RESOURCE).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Missing sample invoice data: " + SAMPLE_JSON_INVOICE_RESOURCE, e);
        }
    }
}

