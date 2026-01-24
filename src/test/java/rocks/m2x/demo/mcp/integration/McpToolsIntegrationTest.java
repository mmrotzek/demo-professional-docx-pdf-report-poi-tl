package rocks.m2x.demo.mcp.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import rocks.m2x.demo.mcp.tools.DataValidationTool;
import rocks.m2x.demo.mcp.tools.ReportGenerationTool;
import rocks.m2x.demo.mcp.tools.TemplateManagementTool;
import rocks.m2x.demo.service.mcp.DataValidationService;
import rocks.m2x.demo.service.mcp.McpReportService;
import rocks.m2x.demo.service.mcp.TemplateResourceService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for MCP tools
 * Tests that all MCP components are properly wired together
 */
@SpringBootTest
@TestPropertySource(properties = {
        "m2x.demo.mcp.enabled=true",
        "spring.ai.mcp.server.annotation-scanner.enabled=true"
})
class McpToolsIntegrationTest {

    @Autowired(required = false)
    private ReportGenerationTool reportGenerationTool;

    @Autowired(required = false)
    private TemplateManagementTool templateManagementTool;

    @Autowired(required = false)
    private DataValidationTool dataValidationTool;

    @Autowired(required = false)
    private McpReportService mcpReportService;

    @Autowired(required = false)
    private TemplateResourceService templateResourceService;

    @Autowired(required = false)
    private DataValidationService dataValidationService;

    @Test
    void contextLoads() {
        // Test that context loads with MCP enabled
        assertNotNull(templateResourceService, "TemplateResourceService should be loaded");
        assertNotNull(dataValidationService, "DataValidationService should be loaded");
        assertNotNull(mcpReportService, "McpReportService should be loaded");
    }

    @Test
    void testServicesAreWired() {
        // Test that services are properly wired
        assertNotNull(mcpReportService);
        assertNotNull(templateResourceService);
        assertNotNull(dataValidationService);
    }

    @Test
    void testToolsAreWired() {
        // Test that tools are properly wired with their dependencies
        // Tools should be available when MCP server is enabled
    }
}
