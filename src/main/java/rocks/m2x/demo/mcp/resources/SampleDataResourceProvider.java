package rocks.m2x.demo.mcp.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;
import rocks.m2x.demo.mcp.model.TemplateMetadata;
import rocks.m2x.demo.service.mcp.TemplateResourceService;

import java.util.HashMap;
import java.util.Map;

/**
 * MCP resource provider for sample data
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SampleDataResourceProvider {
    private final TemplateResourceService templateResourceService;
    private final ObjectMapper objectMapper;

    @McpResource(
            uri = "sample://data/{templateId}",
            name = "Sample Data",
            description = "Example data structures for a template type"
    )
    public String getSampleData(String templateId) {
        try {
            TemplateMetadata metadata = templateResourceService.getTemplateMetadata(templateId);
            
            // Generate a basic sample based on template metadata
            Map<String, Object> sample = generateSampleData(metadata);
            
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(sample);
        } catch (Exception e) {
            log.error("Failed to get sample data", e);
            // Return a generic sample structure
            return generateGenericSample();
        }
    }

    private Map<String, Object> generateSampleData(TemplateMetadata metadata) {
        Map<String, Object> sample = new HashMap<>();
        
        if (metadata == null) {
            return sample;
        }

        // Add sample values for known fields
        if (metadata.getRequiredFields() != null) {
            for (String field : metadata.getRequiredFields()) {
                sample.put(field, getSampleValueForField(field));
            }
        }

        // Add sample HTML field if exists
        if (metadata.getHtmlFields() != null && !metadata.getHtmlFields().isEmpty()) {
            for (String field : metadata.getHtmlFields()) {
                sample.put(field, "<p>Sample <b>HTML</b> content for field: " + field + "</p>");
            }
        }

        return sample;
    }

    private Object getSampleValueForField(String fieldName) {
        String lower = fieldName.toLowerCase();
        if (lower.contains("name") || lower.contains("title")) {
            return "Sample " + fieldName;
        } else if (lower.contains("date") || lower.contains("time")) {
            return "2024-01-15";
        } else if (lower.contains("number") || lower.contains("count") || lower.contains("nr")) {
            return 1;
        } else if (lower.contains("description") || lower.contains("text")) {
            return "Sample description for " + fieldName;
        } else if (lower.contains("list") || lower.contains("items") || lower.contains("array")) {
            return java.util.List.of("Item 1", "Item 2", "Item 3");
        } else {
            return "Sample value for " + fieldName;
        }
    }

    private String generateGenericSample() {
        Map<String, Object> sample = new HashMap<>();
        sample.put("exampleField", "Example Value");
        sample.put("exampleNumber", 123);
        sample.put("exampleDate", "2024-01-15");
        sample.put("exampleHtml", "<p>Example <b>HTML</b> content</p>");
        sample.put("exampleList", java.util.List.of("Item 1", "Item 2"));
        
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(sample);
        } catch (Exception e) {
            return "{\"error\":\"Failed to generate sample data\"}";
        }
    }
}
