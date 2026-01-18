package rocks.m2x.demo.service.mcp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import rocks.m2x.demo.mcp.model.McpReportRequest;
import rocks.m2x.demo.mcp.model.ReportOptions;
import rocks.m2x.demo.service.pdf.DocxToPdfService;
import rocks.m2x.demo.test.TestDataLoader;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for McpReportService
 */
@ExtendWith(MockitoExtension.class)
class McpReportServiceTest {

    @Mock
    private TemplateResourceService templateResourceService;

    @Mock
    private GenericReportService genericReportService;

    @Mock
    private DocxToPdfService docxToPdfService;

    @Mock
    private DataValidationService dataValidationService;

    @InjectMocks
    private McpReportService service;

    private Map<String, Object> sampleData;

    @BeforeEach
    void setUp() {
        sampleData = new HashMap<>();
        sampleData.put("customerName", "Test Customer");
        sampleData.put("invoiceNumber", "INV-001");
    }

    @Test
    void testGenerateReportWithTemplateId() throws Exception {
        byte[] docxBytes = createMinimalDocx();
        Resource mockResource = mock(Resource.class);

        when(templateResourceService.getTemplateResource("test-template")).thenReturn(mockResource);
        ByteArrayOutputStream docxOutput2 = new ByteArrayOutputStream();
        docxOutput2.write(docxBytes);
        when(genericReportService.renderReport(any(), any(), any()))
                .thenReturn(docxOutput2);

        McpReportRequest request = McpReportRequest.builder()
                .templateId("test-template")
                .reportData(sampleData)
                .outputFormat("docx")
                .options(ReportOptions.builder().build())
                .build();

        String result = service.generateReport(request);

        assertNotNull(result);
        verify(templateResourceService).getTemplateResource("test-template");
        verify(genericReportService).renderReport(any(), any(), any());
        verify(docxToPdfService, never()).convertDocxToPdf(any());
    }

    @Test
    void testGenerateReportWithPdfOutput() throws Exception {
        byte[] docxBytes = createMinimalDocx();
        byte[] pdfBytes = new byte[]{1, 2, 3, 4, 5};
        Resource mockResource = mock(Resource.class);
        ByteArrayOutputStream docxOutput = new ByteArrayOutputStream();
        docxOutput.write(docxBytes);
        ByteArrayOutputStream pdfOutput = new ByteArrayOutputStream();
        pdfOutput.write(pdfBytes);

        when(templateResourceService.getTemplateResource("test-template")).thenReturn(mockResource);
        when(genericReportService.renderReport(any(), any(), any())).thenReturn(docxOutput);
        when(docxToPdfService.convertDocxToPdf(any(byte[].class))).thenReturn(pdfOutput);

        McpReportRequest request = McpReportRequest.builder()
                .templateId("test-template")
                .reportData(sampleData)
                .outputFormat("pdf")
                .options(ReportOptions.builder().build())
                .build();

        String result = service.generateReport(request);

        assertNotNull(result);
        verify(docxToPdfService).convertDocxToPdf(any());
    }

    @Test
    void testGenerateReportWithTemplateContent() throws Exception {
        byte[] docxBytes = createMinimalDocx();
        String base64Template = Base64.getEncoder().encodeToString(docxBytes);
        Resource mockResource = mock(Resource.class);
        rocks.m2x.demo.mcp.model.TemplateMetadata metadata = rocks.m2x.demo.mcp.model.TemplateMetadata.builder()
                .id("inline-template")
                .build();

        when(templateResourceService.createTemplateFromBase64(anyString(), anyString(), anyString()))
                .thenReturn(metadata);
        when(templateResourceService.getTemplateResource("inline-template")).thenReturn(mockResource);
        ByteArrayOutputStream docxOutput3 = new ByteArrayOutputStream();
        docxOutput3.write(docxBytes);
        when(genericReportService.renderReport(any(), any(), any()))
                .thenReturn(docxOutput3);

        McpReportRequest request = McpReportRequest.builder()
                .templateContent(base64Template)
                .reportData(sampleData)
                .outputFormat("docx")
                .build();

        String result = service.generateReport(request);

        assertNotNull(result);
        verify(templateResourceService).createTemplateFromBase64(anyString(), anyString(), anyString());
    }

    @Test
    void testGenerateReportMissingTemplate() {
        McpReportRequest request = McpReportRequest.builder()
                .reportData(sampleData)
                .outputFormat("docx")
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            service.generateReport(request);
        });
    }

    @Test
    void testValidateData() {
        rocks.m2x.demo.mcp.model.ValidationResult validationResult = 
                rocks.m2x.demo.mcp.model.ValidationResult.builder()
                        .valid(true)
                        .build();

        when(dataValidationService.validateBasic(any(), any())).thenReturn(validationResult);

        var result = service.validateData("test-template", sampleData);

        assertNotNull(result);
        assertTrue(result.isValid());
    }

    @Test
    void testGenerateReportWithSimpleInvoiceData() throws Exception {
        // Load test data from JSON file
        Map<String, Object> invoiceData = TestDataLoader.loadJsonAsMap("data/simple-invoice.json");
        byte[] docxBytes = TestDataLoader.loadAsBytes("templates/template_invoice.docx");
        String base64Template = Base64.getEncoder().encodeToString(docxBytes);
        
        Resource mockResource = mock(Resource.class);
        rocks.m2x.demo.mcp.model.TemplateMetadata metadata = rocks.m2x.demo.mcp.model.TemplateMetadata.builder()
                .id("invoice-template")
                .build();

        when(templateResourceService.createTemplateFromBase64(anyString(), anyString(), anyString()))
                .thenReturn(metadata);
        when(templateResourceService.getTemplateResource(anyString())).thenReturn(mockResource);
        ByteArrayOutputStream docxOutput = new ByteArrayOutputStream();
        docxOutput.write(docxBytes);
        when(genericReportService.renderReport(any(), any(), any()))
                .thenReturn(docxOutput);

        McpReportRequest request = McpReportRequest.builder()
                .templateContent(base64Template)
                .reportData(invoiceData)
                .outputFormat("docx")
                .options(ReportOptions.builder().build())
                .build();

        String result = service.generateReport(request);

        assertNotNull(result);
        verify(templateResourceService).createTemplateFromBase64(anyString(), anyString(), anyString());
        verify(genericReportService).renderReport(any(), any(), any());
        
        // Verify invoice data structure
        assertEquals("Acme Corporation", invoiceData.get("customerName"));
        assertEquals("INV-2024-001", invoiceData.get("invoiceNumber"));
        assertNotNull(invoiceData.get("items"));
    }

    @Test
    void testGenerateReportWithComplexInvoiceData() throws Exception {
        // Load test data from JSON file
        Map<String, Object> invoiceData = TestDataLoader.loadJsonAsMap("data/complex-invoice.json");
        byte[] docxBytes = TestDataLoader.loadAsBytes("templates/template_invoice.docx");
        String base64Template = Base64.getEncoder().encodeToString(docxBytes);
        
        Resource mockResource = mock(Resource.class);
        rocks.m2x.demo.mcp.model.TemplateMetadata metadata = rocks.m2x.demo.mcp.model.TemplateMetadata.builder()
                .id("invoice-template")
                .build();

        when(templateResourceService.createTemplateFromBase64(anyString(), anyString(), anyString()))
                .thenReturn(metadata);
        when(templateResourceService.getTemplateResource(anyString())).thenReturn(mockResource);
        ByteArrayOutputStream docxOutput = new ByteArrayOutputStream();
        docxOutput.write(docxBytes);
        ByteArrayOutputStream pdfOutput = new ByteArrayOutputStream();
        pdfOutput.write(new byte[]{1, 2, 3, 4, 5});
        when(genericReportService.renderReport(any(), any(), any())).thenReturn(docxOutput);
        when(docxToPdfService.convertDocxToPdf(any(byte[].class))).thenReturn(pdfOutput);

        McpReportRequest request = McpReportRequest.builder()
                .templateContent(base64Template)
                .reportData(invoiceData)
                .outputFormat("pdf")
                .options(ReportOptions.builder().build())
                .build();

        String result = service.generateReport(request);

        assertNotNull(result);
        verify(docxToPdfService).convertDocxToPdf(any());
        
        // Verify complex invoice data structure
        assertTrue(invoiceData.containsKey("customer"));
        @SuppressWarnings("unchecked")
        Map<String, Object> customer = (Map<String, Object>) invoiceData.get("customer");
        assertEquals("Global Industries Ltd.", customer.get("name"));
    }

    @Test
    void testValidateDataWithInvoiceData() {
        // Load test data from JSON file
        Map<String, Object> invoiceData = TestDataLoader.loadJsonAsMap("data/simple-invoice.json");
        
        rocks.m2x.demo.mcp.model.ValidationResult validationResult = 
                rocks.m2x.demo.mcp.model.ValidationResult.builder()
                        .valid(true)
                        .build();

        when(dataValidationService.validateBasic(any(), any())).thenReturn(validationResult);

        var result = service.validateData("invoice-template", invoiceData);

        assertNotNull(result);
        assertTrue(result.isValid());
        verify(dataValidationService).validateBasic(any(Map.class), any());
    }

    private byte[] createMinimalDocx() {
        byte[] bytes = new byte[100];
        bytes[0] = 0x50;
        bytes[1] = 0x4B;
        bytes[2] = 0x03;
        bytes[3] = 0x04;
        return bytes;
    }
}
