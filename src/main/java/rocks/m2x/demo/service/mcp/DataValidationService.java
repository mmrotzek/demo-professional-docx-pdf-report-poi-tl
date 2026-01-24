package rocks.m2x.demo.service.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rocks.m2x.demo.mcp.model.ValidationError;
import rocks.m2x.demo.mcp.model.ValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service for validating report data against JSON schemas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataValidationService {
    private final ObjectMapper objectMapper;
    private final SchemaRegistry schemaRegistry = SchemaRegistry.withDefaultDialect(
            SpecificationVersion.DRAFT_7
    );

    /**
     * Validate data against a JSON schema
     *
     * @param data Data to validate
     * @param schema JSON schema to validate against
     * @return ValidationResult with validation status and errors
     */
    public ValidationResult validate(Map<String, Object> data, JsonNode schema) {
        try {
            JsonNode dataNode = objectMapper.valueToTree(data);
            
            // Convert JsonNode to string for schema loading
            String schemaString = objectMapper.writeValueAsString(schema);
            
            // Create schema from string using SchemaLocation
            Schema jsonSchema = schemaRegistry.getSchema(
                    SchemaLocation.of("urn:memory:schema"), 
                    schemaString,
                    InputFormat.JSON
            );

            // Validate data
            List<com.networknt.schema.Error> validationErrors = jsonSchema.validate(
                    objectMapper.writeValueAsString(dataNode),
                    InputFormat.JSON
            );

            List<ValidationError> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            for (com.networknt.schema.Error error : validationErrors) {
                ValidationError validationError = ValidationError.builder()
                        .field(error.getInstanceLocation().toString())
                        .message(error.getMessage())
                        .code("VALIDATION_ERROR")
                        .build();

                // Determine if it's an error or warning based on severity
                if (error.getMessage().toLowerCase().contains("warning")) {
                    warnings.add(validationError.getMessage());
                } else {
                    errors.add(validationError);
                }
            }

            boolean isValid = errors.isEmpty();

            return ValidationResult.builder()
                    .valid(isValid)
                    .errors(errors)
                    .warnings(warnings)
                    .normalizedData(isValid ? data : null)
                    .build();

        } catch (Exception e) {
            log.error("Validation error", e);
            return ValidationResult.builder()
                    .valid(false)
                    .errors(List.of(ValidationError.builder()
                            .field("$")
                            .message("Schema validation failed")
                            .code("VALIDATION_EXCEPTION")
                            .build()))
                    .build();
        }
    }

    /**
     * Validate data against a template's schema (basic validation)
     * This performs basic checks without a full JSON schema
     *
     * @param data Data to validate
     * @param requiredFields Required field names
     * @return ValidationResult with validation status and errors
     */
    public ValidationResult validateBasic(Map<String, Object> data, Set<String> requiredFields) {
        List<ValidationError> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (requiredFields != null && !requiredFields.isEmpty()) {
            for (String requiredField : requiredFields) {
                if (!data.containsKey(requiredField) || data.get(requiredField) == null) {
                    errors.add(ValidationError.builder()
                            .field(requiredField)
                            .message("Required field is missing: " + requiredField)
                            .code("REQUIRED")
                            .build());
                }
            }
        }

        boolean isValid = errors.isEmpty();

        return ValidationResult.builder()
                .valid(isValid)
                .errors(errors)
                .warnings(warnings)
                .normalizedData(isValid ? data : null)
                .build();
    }

    /**
     * Normalize data (convert types, apply defaults, etc.)
     *
     * @param data Data to normalize
     * @return Normalized data
     */
    public Map<String, Object> normalizeData(Map<String, Object> data) {
        // Basic normalization - convert JSON structure to Map
        // This can be extended with more sophisticated normalization logic
        return objectMapper.convertValue(
                objectMapper.valueToTree(data),
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
        );
    }
}
