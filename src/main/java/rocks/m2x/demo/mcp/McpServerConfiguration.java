package rocks.m2x.demo.mcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import rocks.m2x.demo.config.McpProperties;

/**
 * Configuration for MCP server
 * 
 * The MCP server is automatically configured by Spring AI MCP starter.
 * This configuration class enables MCP properties and provides any additional setup.
 */
@Configuration
@EnableConfigurationProperties(McpProperties.class)
@Slf4j
public class McpServerConfiguration {

    public McpServerConfiguration(McpProperties mcpProperties) {
        if (mcpProperties.isEnabled()) {
            log.info("MCP server enabled - Server: {}, Version: {}", 
                    mcpProperties.getServerName(), 
                    mcpProperties.getServerVersion());
            log.info("MCP transports - STDIO: {}, HTTP: {}", 
                    mcpProperties.isStdioEnabled(), 
                    mcpProperties.isHttpEnabled());
            if (mcpProperties.isHttpEnabled()) {
                log.info("MCP HTTP endpoint: {}", mcpProperties.getHttpPath());
            }
        } else {
            log.info("MCP server disabled");
        }
    }
}
