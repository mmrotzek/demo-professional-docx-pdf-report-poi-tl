package rocks.m2x.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for MCP server
 */
@ConfigurationProperties(prefix = "m2x.demo.mcp")
@Getter
@Setter
public class McpProperties {
    /**
     * Enable MCP server
     */
    boolean enabled = true;

    /**
     * Maximum size for base64 template content (in bytes)
     */
    long maxTemplateSize = 10 * 1024 * 1024; // 10MB default

    /**
     * Temporary file storage directory
     */
    String tempStorageDir = System.getProperty("java.io.tmpdir");

    /**
     * Time-to-live for temporary template files (default: 1 hour)
     */
    Duration templateTtl = Duration.ofHours(1);

    /**
     * Enable template content validation
     */
    boolean validateTemplateContent = true;

    /**
     * Server name for MCP protocol
     */
    String serverName = "document-generator";

    /**
     * Server version
     */
    String serverVersion = "1.0.0";

    /**
     * Enable STDIO transport
     */
    boolean stdioEnabled = true;

    /**
     * Enable HTTP/SSE transport
     */
    boolean httpEnabled = true;

    /**
     * HTTP endpoint path for MCP messages
     */
    String httpPath = "/mcp/message";

    /**
     * Rate limiting configuration
     */
    RateLimitConfig rateLimit = new RateLimitConfig();

    @Getter
    @Setter
    public static class RateLimitConfig {
        /**
         * Enable rate limiting
         */
        boolean enabled = false;

        /**
         * Maximum requests per minute
         */
        int requestsPerMinute = 60;
    }
}
