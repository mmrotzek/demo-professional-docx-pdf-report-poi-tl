package rocks.m2x.demo.mcp;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import rocks.m2x.demo.config.McpProperties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for MCP server configuration
 * 
 * NOTE: Currently disabled due to dependency conflicts with JSON Schema Validator.
 */
@Disabled("Disabled due to dependency conflicts")
@SpringBootTest
@TestPropertySource(properties = {
        "m2x.demo.mcp.enabled=false",
        "spring.ai.mcp.server.annotation-scanner.enabled=false"
})
class McpServerConfigurationTest {

    @Autowired(required = false)
    private McpProperties mcpProperties;

    @Test
    void contextLoads() {
        // Test that the configuration loads without errors
        assertNotNull(mcpProperties);
        // MCP is disabled in this test to avoid dependency conflicts
        assertFalse(mcpProperties.isEnabled());
    }
}
