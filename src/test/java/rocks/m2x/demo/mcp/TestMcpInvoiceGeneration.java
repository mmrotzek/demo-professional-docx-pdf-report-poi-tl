package rocks.m2x.demo.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import rocks.m2x.demo.mcp.tools.ReportGenerationTool;
import rocks.m2x.demo.test.TestDataLoader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test MCP interface with invoice template and data
 * Generates DOCX only (no PDF conversion needed)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "m2x.demo.mcp.enabled=true",
        "m2x.demo.mcp.http-enabled=true",
        "m2x.demo.mcp.http-path=/mcp/message",
        "spring.ai.mcp.server.annotation-scanner.enabled=true"
})
class TestMcpInvoiceGeneration {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReportGenerationTool reportGenerationTool;

    private String baseUrl;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
    }

    /**
     * Create HTTP headers with proper Accept headers for Streamable HTTP
     */
    private HttpHeaders createMcpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Streamable HTTP requires specific Accept headers
        headers.setAccept(java.util.Arrays.asList(
                org.springframework.http.MediaType.TEXT_EVENT_STREAM,
                org.springframework.http.MediaType.APPLICATION_JSON
        ));
        return headers;
    }

    @Test
    void testGenerateInvoiceDocxViaHttp() throws Exception {
        // Step 1: Initialize session
        Map<String, Object> initRequest = new HashMap<>();
        initRequest.put("jsonrpc", "2.0");
        initRequest.put("method", "initialize");
        initRequest.put("id", 0);
        Map<String, Object> initParams = new HashMap<>();
        initParams.put("protocolVersion", "2025-03-26");
        initParams.put("capabilities", new HashMap<>());
        Map<String, Object> clientInfo = new HashMap<>();
        clientInfo.put("name", "test-client");
        clientInfo.put("version", "1.0.0");
        initParams.put("clientInfo", clientInfo);
        initRequest.put("params", initParams);

        HttpHeaders initHeaders = createMcpHeaders();
        HttpEntity<Map<String, Object>> initEntity = new HttpEntity<>(initRequest, initHeaders);
        
        ResponseEntity<String> initResponse = restTemplate.exchange(
                baseUrl + "/mcp/message",
                HttpMethod.POST,
                initEntity,
                String.class
        );

        assertEquals(HttpStatus.OK, initResponse.getStatusCode());
        
        // Extract session ID from response headers
        String sessionId = initResponse.getHeaders().getFirst("Mcp-Session-Id");
        if (sessionId == null) {
            // Try case-insensitive
            for (String headerName : initResponse.getHeaders().keySet()) {
                if (headerName.equalsIgnoreCase("Mcp-Session-Id")) {
                    sessionId = initResponse.getHeaders().getFirst(headerName);
                    break;
                }
            }
        }
        assertNotNull(sessionId, "Session ID should be present in response headers");
        System.out.println("Session ID: " + sessionId);

        // Step 2: Send initialized notification
        Map<String, Object> initializedRequest = new HashMap<>();
        initializedRequest.put("jsonrpc", "2.0");
        initializedRequest.put("method", "notifications/initialized");
        initializedRequest.put("params", new HashMap<>());

        HttpHeaders initializedHeaders = createMcpHeaders();
        initializedHeaders.set("Mcp-Session-Id", sessionId);
        HttpEntity<Map<String, Object>> initializedEntity = new HttpEntity<>(initializedRequest, initializedHeaders);
        
        restTemplate.exchange(
                baseUrl + "/mcp/message",
                HttpMethod.POST,
                initializedEntity,
                String.class
        );

        // Step 3: Load invoice template and test data
        byte[] templateBytes = TestDataLoader.loadAsBytes("templates/template_invoice.docx");
        String base64Template = Base64.getEncoder().encodeToString(templateBytes);
        Map<String, Object> reportData = TestDataLoader.loadJsonAsMap("data/simple-invoice.json");

        Map<String, Object> options = new HashMap<>();
        options.put("readonly", true);
        options.put("draft", false);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("templateContent", base64Template);
        arguments.put("reportData", reportData);
        arguments.put("outputFormat", "docx"); // DOCX only, no PDF
        arguments.put("options", options);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "generate_report");
        params.put("arguments", arguments);

        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("method", "tools/call");
        request.put("id", 1);
        request.put("params", params);

        HttpHeaders headers = createMcpHeaders();
        headers.set("Mcp-Session-Id", sessionId);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        // Streamable HTTP may return a text/event-stream body, so we read as String and parse the first data frame
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/mcp/message",
                HttpMethod.POST,
                entity,
                String.class
        );

        // Debug: print request and response
        System.out.println("Request: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Parse event-stream style response - extract JSON from the first data event if present
        String responseBody = response.getBody();
        
        Map<String, Object> responseJson = null;

        // Event-stream format is multi-line: "id:...\\nevent:...\\ndata:{json}\\n\\n"
        // Extract the line starting with "data:"
        String[] lines = responseBody.split("\n");
        String jsonStr = null;
        for (String line : lines) {
            if (line.startsWith("data: ")) {
                jsonStr = line.substring(6).trim(); // Remove "data: " prefix
                break;
            } else if (line.startsWith("data:")) {
                jsonStr = line.substring(5).trim(); // Remove "data:" prefix
                break;
            }
        }
        
        if (jsonStr == null || jsonStr.isEmpty()) {
            // Try parsing as direct JSON if no SSE format detected
            jsonStr = responseBody.trim();
        }
        
        responseJson = objectMapper.readValue(jsonStr, Map.class);
        
        assertNotNull(responseJson, "Could not parse response as JSON");
        assertEquals("2.0", responseJson.get("jsonrpc"));

        // Check result
        Object resultObj = responseJson.get("result");
        assertNotNull(resultObj, "Result should not be null");

        String documentBase64 = null;

        // Try different response structures
        if (resultObj instanceof Map) {
            Map<?, ?> result = (Map<?, ?>) resultObj;

            // Check if there's an error
            if (result.containsKey("error")) {
                fail("Error in response: " + result.get("error"));
            }

            // Try to find document in various possible locations
            if (result.containsKey("document")) {
                documentBase64 = (String) result.get("document");
            } else if (result.containsKey("content")) {
                Object content = result.get("content");
                if (content instanceof java.util.List) {
                    java.util.List<?> contentList = (java.util.List<?>) content;
                    if (!contentList.isEmpty() && contentList.get(0) instanceof Map) {
                        Map<?, ?> firstContent = (Map<?, ?>) contentList.get(0);
                        if (firstContent.containsKey("text")) {
                            // The "text" field contains a JSON string, parse it
                            String textJson = (String) firstContent.get("text");
                            Map<String, Object> textData = objectMapper.readValue(textJson, Map.class);
                            documentBase64 = (String) textData.get("document");
                        }
                    }
                } else if (content instanceof Map) {
                    Map<?, ?> contentMap = (Map<?, ?>) content;
                    if (contentMap.containsKey("text")) {
                        // The "text" field contains a JSON string, parse it
                        String textJson = (String) contentMap.get("text");
                        Map<String, Object> textData = objectMapper.readValue(textJson, Map.class);
                        documentBase64 = (String) textData.get("document");
                    }
                }
            }
        }

        assertNotNull(documentBase64, "Could not find document in response. Response: " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response.getBody()));

        // Decode and save the document
        byte[] documentBytes = Base64.getDecoder().decode(documentBase64);
        assertTrue(documentBytes.length > 0, "Document should not be empty");

        // Save to file for verification
        String outputPath = "target/test-invoice-http.docx";
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(documentBytes);
        }
        System.out.println("Generated DOCX saved to: " + outputPath);
        System.out.println("Document size: " + documentBytes.length + " bytes");

        // Verify it's a valid DOCX (ZIP signature)
        assertEquals(0x50, documentBytes[0] & 0xFF, "Should start with ZIP signature");
        assertEquals(0x4B, documentBytes[1] & 0xFF, "Should start with ZIP signature");
    }

    @Test
    void testGenerateInvoiceDocxDirect() throws Exception {
        // Test directly using the tool (bypassing HTTP)
        byte[] templateBytes = TestDataLoader.loadAsBytes("templates/template_invoice.docx");
        String base64Template = Base64.getEncoder().encodeToString(templateBytes);
        Map<String, Object> reportData = TestDataLoader.loadJsonAsMap("data/simple-invoice.json");

        Map<String, Object> options = new HashMap<>();
        options.put("readonly", true);
        options.put("draft", false);

        ReportGenerationTool.GenerateReportResult result = reportGenerationTool.generateReport(
                null, // templateId
                base64Template, // templateContent
                reportData,
                "docx", // outputFormat
                options
        );

        assertTrue(result.isSuccess(), "Report generation should succeed: " + result.getMessage());
        assertNotNull(result.getDocument(), "Document should not be null");
        assertEquals("docx", result.getFormat(), "Format should be docx");

        // Decode and save the document
        byte[] documentBytes = Base64.getDecoder().decode(result.getDocument());
        assertTrue(documentBytes.length > 0, "Document should not be empty");

        // Save to file for verification
        String outputPath = "target/test-invoice-direct.docx";
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(documentBytes);
        }
        System.out.println("Generated DOCX saved to: " + outputPath);
        System.out.println("Document size: " + documentBytes.length + " bytes");

        // Verify it's a valid DOCX (ZIP signature)
        assertEquals(0x50, documentBytes[0] & 0xFF, "Should start with ZIP signature");
        assertEquals(0x4B, documentBytes[1] & 0xFF, "Should start with ZIP signature");
    }

    @Test
    void testGenerateComplexInvoiceDocx() throws Exception {
        // Test with complex invoice data
        byte[] templateBytes = TestDataLoader.loadAsBytes("templates/template_invoice.docx");
        String base64Template = Base64.getEncoder().encodeToString(templateBytes);
        Map<String, Object> reportData = TestDataLoader.loadJsonAsMap("data/complex-invoice.json");

        Map<String, Object> options = new HashMap<>();
        options.put("readonly", false);
        options.put("draft", true);

        ReportGenerationTool.GenerateReportResult result = reportGenerationTool.generateReport(
                null,
                base64Template,
                reportData,
                "docx",
                options
        );

        assertTrue(result.isSuccess(), "Report generation should succeed: " + result.getMessage());
        assertNotNull(result.getDocument(), "Document should not be null");

        // Decode and save the document
        byte[] documentBytes = Base64.getDecoder().decode(result.getDocument());
        assertTrue(documentBytes.length > 0, "Document should not be empty");

        // Save to file for verification
        String outputPath = "target/test-complex-invoice.docx";
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(documentBytes);
        }
        System.out.println("Generated DOCX saved to: " + outputPath);
        System.out.println("Document size: " + documentBytes.length + " bytes");
    }
}
