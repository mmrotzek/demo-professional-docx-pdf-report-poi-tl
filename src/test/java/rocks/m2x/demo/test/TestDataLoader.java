package rocks.m2x.demo.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utility class to load JSON test data files from resources
 */
@Slf4j
public class TestDataLoader {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Load JSON test data file from resources
     *
     * @param resourcePath Path relative to src/test/resources (e.g., "data/simple-invoice.json")
     * @param clazz        Target class type
     * @return Parsed JSON object
     */
    public static <T> T loadJson(String resourcePath, Class<T> clazz) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            try (InputStream inputStream = resource.getInputStream()) {
                String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                return objectMapper.readValue(content, clazz);
            }
        } catch (IOException e) {
            log.error("Failed to load test data from resource: {}", resourcePath, e);
            throw new RuntimeException("Failed to load test data from resource: " + resourcePath, e);
        }
    }

    /**
     * Load JSON test data file as Map
     *
     * @param resourcePath Path relative to src/test/resources
     * @return Map representation of JSON
     */
    @SuppressWarnings("unchecked")
    public static java.util.Map<String, Object> loadJsonAsMap(String resourcePath) {
        return loadJson(resourcePath, java.util.Map.class);
    }

    /**
     * Load file content as String
     *
     * @param resourcePath Path relative to src/test/resources
     * @return File content as String
     */
    public static String loadAsString(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            try (InputStream inputStream = resource.getInputStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.error("Failed to load resource as string: {}", resourcePath, e);
            throw new RuntimeException("Failed to load resource as string: " + resourcePath, e);
        }
    }

    /**
     * Load binary file as byte array
     *
     * @param resourcePath Path relative to src/test/resources
     * @return File content as byte array
     */
    public static byte[] loadAsBytes(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            try (InputStream inputStream = resource.getInputStream()) {
                return inputStream.readAllBytes();
            }
        } catch (IOException e) {
            log.error("Failed to load resource as bytes: {}", resourcePath, e);
            throw new RuntimeException("Failed to load resource as bytes: " + resourcePath, e);
        }
    }
}
