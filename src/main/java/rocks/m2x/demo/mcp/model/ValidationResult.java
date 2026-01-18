package rocks.m2x.demo.mcp.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Result of data validation
 */
@Data
@Builder
public class ValidationResult {
    /**
     * Whether the data is valid
     */
    boolean valid;

    /**
     * List of validation errors
     */
    @Builder.Default
    List<ValidationError> errors = List.of();

    /**
     * List of validation warnings (non-blocking issues)
     */
    @Builder.Default
    List<String> warnings = List.of();

    /**
     * Normalized/cleaned data (if normalization was performed)
     */
    Map<String, Object> normalizedData;
}
