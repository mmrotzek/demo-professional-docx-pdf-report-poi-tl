package rocks.m2x.demo.service.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rocks.m2x.demo.mcp.model.ValidationError;
import rocks.m2x.demo.mcp.model.ValidationResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DataValidationService
 */
class DataValidationServiceTest {

    private DataValidationService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new DataValidationService(objectMapper);
    }

    @Test
    void testValidateBasicWithRequiredFields() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test");
        data.put("email", "test@example.com");

        Set<String> requiredFields = Set.of("name", "email", "phone");

        ValidationResult result = service.validateBasic(data, requiredFields);

        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals("phone", result.getErrors().get(0).getField());
        assertEquals("REQUIRED", result.getErrors().get(0).getCode());
    }

    @Test
    void testValidateBasicWithAllRequiredFields() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test");
        data.put("email", "test@example.com");
        data.put("phone", "123456789");

        Set<String> requiredFields = Set.of("name", "email", "phone");

        ValidationResult result = service.validateBasic(data, requiredFields);

        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
        assertNotNull(result.getNormalizedData());
    }

    @Test
    void testValidateBasicWithNullValues() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test");
        data.put("email", null);

        Set<String> requiredFields = Set.of("name", "email");

        ValidationResult result = service.validateBasic(data, requiredFields);

        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals("email", result.getErrors().get(0).getField());
    }

    @Test
    void testValidateBasicWithNoRequiredFields() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test");

        ValidationResult result = service.validateBasic(data, null);

        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
    }

    @Test
    void testValidateWithJsonSchema() throws Exception {
        // Create a simple JSON schema
        String schemaJson = """
                {
                  "type": "object",
                  "properties": {
                    "name": {
                      "type": "string"
                    },
                    "age": {
                      "type": "number",
                      "minimum": 0
                    }
                  },
                  "required": ["name", "age"]
                }
                """;

        JsonNode schema = objectMapper.readTree(schemaJson);

        // Valid data
        Map<String, Object> validData = new HashMap<>();
        validData.put("name", "Test");
        validData.put("age", 25);

        ValidationResult result = service.validate(validData, schema);
        assertTrue(result.isValid());

        // Invalid data - missing required field
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("name", "Test");

        ValidationResult invalidResult = service.validate(invalidData, schema);
        assertFalse(invalidResult.isValid());
        assertTrue(invalidResult.getErrors().size() > 0);
    }

    @Test
    void testNormalizeData() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test");
        data.put("age", 25);
        data.put("active", true);

        Map<String, Object> normalized = service.normalizeData(data);

        assertNotNull(normalized);
        assertEquals("Test", normalized.get("name"));
        assertEquals(25, normalized.get("age"));
        assertEquals(true, normalized.get("active"));
    }
}
