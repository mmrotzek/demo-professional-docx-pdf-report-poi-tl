package rocks.m2x.demo.mcp.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Metadata about a template
 */
@Data
@Builder
public class TemplateMetadata {
    /**
     * Unique identifier for the template
     */
    String id;

    /**
     * Human-readable template name
     */
    String name;

    /**
     * Description of the template's purpose and structure
     */
    String description;

    /**
     * Template version
     */
    String version;

    /**
     * Timestamp when template was uploaded/created
     */
    LocalDateTime uploaded;

    /**
     * JSON schema defining expected data structure
     */
    JsonNode dataSchema;

    /**
     * Set of required field names
     */
    Set<String> requiredFields;

    /**
     * Set of fields that support HTML rendering
     */
    Set<String> htmlFields;
}
