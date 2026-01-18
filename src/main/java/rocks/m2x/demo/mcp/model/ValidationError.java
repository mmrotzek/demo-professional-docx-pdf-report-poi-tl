package rocks.m2x.demo.mcp.model;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a validation error
 */
@Data
@Builder
public class ValidationError {
    /**
     * Field path where the error occurred (e.g., "groups[0].controls[1].name")
     */
    String field;

    /**
     * Error message
     */
    String message;

    /**
     * Error code (e.g., "REQUIRED", "TYPE_MISMATCH", "INVALID_FORMAT")
     */
    String code;

    /**
     * The value that failed validation (if applicable)
     */
    Object rejectedValue;
}
