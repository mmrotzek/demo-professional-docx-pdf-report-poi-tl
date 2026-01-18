package rocks.m2x.demo.mcp.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rocks.m2x.demo.service.mcp.McpReportService;
import rocks.m2x.demo.test.TestDataLoader;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ReportGenerationTool
 */
@ExtendWith(MockitoExtension.class)
class ReportGenerationToolTest {

    @Mock
    private McpReportService mcpReportService;

    @InjectMocks
    private ReportGenerationTool tool;

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
        String base64Docx = Base64.getEncoder().encodeToString(docxBytes);

        when(mcpReportService.generateReport(any())).thenReturn(base64Docx);

        ReportGenerationTool.GenerateReportResult result = tool.generateReport(
                "test-template",
                null,
                sampleData,
                "docx",
                null
        );

        assertTrue(result.isSuccess());
        assertNotNull(result.getDocument());
        assertEquals("docx", result.getFormat());
    }

    @Test
    void testGenerateReportWithTemplateContent() throws Exception {
        byte[] docxBytes = createMinimalDocx();
        String base64Template = Base64.getEncoder().encodeToString(docxBytes);
        String base64Docx = Base64.getEncoder().encodeToString(docxBytes);

        when(mcpReportService.generateReport(any())).thenReturn(base64Docx);

        ReportGenerationTool.GenerateReportResult result = tool.generateReport(
                null,
                base64Template,
                sampleData,
                "pdf",
                null
        );

        assertTrue(result.isSuccess());
        assertNotNull(result.getDocument());
        assertEquals("pdf", result.getFormat());
    }

    @Test
    void testGenerateReportWithOptions() throws Exception {
        byte[] docxBytes = createMinimalDocx();
        String base64Docx = Base64.getEncoder().encodeToString(docxBytes);

        when(mcpReportService.generateReport(any())).thenReturn(base64Docx);

        Map<String, Object> options = new HashMap<>();
        options.put("readonly", true);
        options.put("draft", false);
        options.put("htmlFields", "description,notes");

        ReportGenerationTool.GenerateReportResult result = tool.generateReport(
                "test-template",
                null,
                sampleData,
                "docx",
                options
        );

        assertTrue(result.isSuccess());
    }

    @Test
    void testGenerateReportFailure() throws Exception {
        when(mcpReportService.generateReport(any())).thenThrow(new RuntimeException("Generation failed"));

        ReportGenerationTool.GenerateReportResult result = tool.generateReport(
                "test-template",
                null,
                sampleData,
                "docx",
                null
        );

        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
        // Error message is now generic for security (doesn't expose exception details)
        assertTrue(result.getMessage().contains("Failed to generate report"));
    }

    @Test
    void testGenerateReportWithSimpleInvoiceData() throws Exception {
        // Load test data from JSON file
        Map<String, Object> invoiceData = TestDataLoader.loadJsonAsMap("data/simple-invoice.json");
        byte[] docxBytes = TestDataLoader.loadAsBytes("templates/template_invoice.docx");
        String base64Template = Base64.getEncoder().encodeToString(docxBytes);
        String base64Docx = Base64.getEncoder().encodeToString(docxBytes);

        when(mcpReportService.generateReport(any())).thenReturn(base64Docx);

        ReportGenerationTool.GenerateReportResult result = tool.generateReport(
                null,
                base64Template,
                invoiceData,
                "docx",
                null
        );

        assertTrue(result.isSuccess());
        assertNotNull(result.getDocument());
        assertEquals("docx", result.getFormat());
        
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
        String base64Docx = Base64.getEncoder().encodeToString(docxBytes);

        when(mcpReportService.generateReport(any())).thenReturn(base64Docx);

        ReportGenerationTool.GenerateReportResult result = tool.generateReport(
                null,
                base64Template,
                invoiceData,
                "pdf",
                null
        );

        assertTrue(result.isSuccess());
        assertNotNull(result.getDocument());
        assertEquals("pdf", result.getFormat());
        
        // Verify complex invoice data structure
        assertTrue(invoiceData.containsKey("customer"));
        @SuppressWarnings("unchecked")
        Map<String, Object> customer = (Map<String, Object>) invoiceData.get("customer");
        assertEquals("Global Industries Ltd.", customer.get("name"));
        assertNotNull(customer.get("address"));
    }

    /**
     * Create a minimal valid DOCX file
     */
    private byte[] createMinimalDocx() {
        // Minimal ZIP signature for a valid DOCX
        byte[] bytes = new byte[100];
        bytes[0] = 0x50;
        bytes[1] = 0x4B;
        bytes[2] = 0x03;
        bytes[3] = 0x04;
        return bytes;
    }
}
