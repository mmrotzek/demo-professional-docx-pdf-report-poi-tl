package rocks.m2x.demo.mcp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import rocks.m2x.demo.test.TestDataLoader;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HTTP endpoint integration tests for MCP server
 * 
 * Tests the MCP HTTP endpoint at /mcp/message using JSON-RPC 2.0 protocol
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "m2x.demo.mcp.enabled=true",
        "m2x.demo.mcp.http-enabled=true",
        "m2x.demo.mcp.http-path=/mcp/message",
        "spring.ai.mcp.server.annotation-scanner.enabled=true"
})
class McpHttpEndpointIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
    }

    @Test
    void testListTools() {
        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("method", "tools/list");
        request.put("id", 1);
        request.put("params", new HashMap<>());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/mcp/message",
                HttpMethod.POST,
                entity,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("2.0", response.getBody().get("jsonrpc"));
        assertNotNull(response.getBody().get("result"));
    }

    @Test
    void testListTemplates() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "list_templates");
        params.put("arguments", new HashMap<>());

        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("method", "tools/call");
        request.put("id", 2);
        request.put("params", params);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/mcp/message",
                HttpMethod.POST,
                entity,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("2.0", response.getBody().get("jsonrpc"));
    }

    @Test
    void testProvideTemplate() throws Exception {
        // Load invoice template
        byte[] templateBytes = TestDataLoader.loadAsBytes("templates/template_invoice.docx");
        String base64Template = Base64.getEncoder().encodeToString(templateBytes);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("templateName", "invoice-template-test");
        arguments.put("templateContent", base64Template);
        arguments.put("description", "Invoice template for testing");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "provide_template");
        params.put("arguments", arguments);

        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("method", "tools/call");
        request.put("id", 3);
        request.put("params", params);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/mcp/message",
                HttpMethod.POST,
                entity,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("2.0", response.getBody().get("jsonrpc"));
    }

    @Test
    void testValidateData() throws Exception {
        // Load test data
        Map<String, Object> reportData = TestDataLoader.loadJsonAsMap("data/simple-invoice.json");

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("templateId", "invoice-template");
        arguments.put("reportData", reportData);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "validate_data");
        params.put("arguments", arguments);

        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("method", "tools/call");
        request.put("id", 4);
        request.put("params", params);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/mcp/message",
                HttpMethod.POST,
                entity,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("2.0", response.getBody().get("jsonrpc"));
    }

    @Test
    void testGenerateReportDocx() throws Exception {
        // Load invoice template and test data
        byte[] templateBytes = TestDataLoader.loadAsBytes("templates/template_invoice.docx");
        String base64Template = Base64.getEncoder().encodeToString(templateBytes);
        Map<String, Object> reportData = TestDataLoader.loadJsonAsMap("data/simple-invoice.json");

        Map<String, Object> options = new HashMap<>();
        options.put("readonly", true);
        options.put("draft", false);

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("templateContent", base64Template);
        arguments.put("reportData", reportData);
        arguments.put("outputFormat", "docx");
        arguments.put("options", options);

        Map<String, Object> params = new HashMap<>();
        params.put("name", "generate_report");
        params.put("arguments", arguments);

        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("method", "tools/call");
        request.put("id", 5);
        request.put("params", params);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/mcp/message",
                HttpMethod.POST,
                entity,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("2.0", response.getBody().get("jsonrpc"));
    }

    @Test
    void testInvalidRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("method", "invalid_method");
        request.put("id", 99);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/mcp/message",
                HttpMethod.POST,
                entity,
                Map.class
        );

        // Should return error response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("2.0", response.getBody().get("jsonrpc"));
        // Should have error field for invalid method
        assertTrue(response.getBody().containsKey("error") || 
                  (response.getBody().containsKey("result") && 
                   response.getBody().get("result") instanceof Map &&
                   ((Map<?, ?>) response.getBody().get("result")).containsKey("error")));
    }
}
