package rocks.m2x.demo.mcp.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rocks.m2x.demo.mcp.model.TemplateMetadata;
import rocks.m2x.demo.service.mcp.TemplateResourceService;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TemplateManagementTool
 */
@ExtendWith(MockitoExtension.class)
class TemplateManagementToolTest {

    @Mock
    private TemplateResourceService templateResourceService;

    @InjectMocks
    private TemplateManagementTool tool;

    private String base64Template;

    @BeforeEach
    void setUp() {
        byte[] docxBytes = createMinimalDocx();
        base64Template = Base64.getEncoder().encodeToString(docxBytes);
    }

    @Test
    void testProvideTemplate() throws Exception {
        TemplateMetadata metadata = TemplateMetadata.builder()
                .id("test-template")
                .name("test-template")
                .description("Test template")
                .version("1.0")
                .uploaded(LocalDateTime.now())
                .build();

        when(templateResourceService.createTemplateFromBase64(anyString(), anyString(), anyString()))
                .thenReturn(metadata);

        TemplateManagementTool.ProvideTemplateResult result = tool.provideTemplate(
                "test-template",
                base64Template,
                "Test template description"
        );

        assertTrue(result.isSuccess());
        assertEquals("test-template", result.getTemplateId());
        assertTrue(result.getTemplateUri().contains("test-template"));
        assertNotNull(result.getMetadata());
    }

    @Test
    void testProvideTemplateFailure() throws Exception {
        when(templateResourceService.createTemplateFromBase64(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Template creation failed"));

        TemplateManagementTool.ProvideTemplateResult result = tool.provideTemplate(
                "test-template",
                base64Template,
                "Test"
        );

        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
    }

    @Test
    void testListTemplates() {
        Map<String, TemplateMetadata> templates = new HashMap<>();
        templates.put("template1", TemplateMetadata.builder()
                .id("template1")
                .name("Template 1")
                .build());
        templates.put("template2", TemplateMetadata.builder()
                .id("template2")
                .name("Template 2")
                .build());

        when(templateResourceService.listTemplates()).thenReturn(templates);

        TemplateManagementTool.ListTemplatesResult result = tool.listTemplates();

        assertTrue(result.isSuccess());
        assertEquals(2, result.getCount());
        assertEquals(2, result.getTemplates().size());
    }

    /**
     * Create a minimal valid DOCX file
     */
    private byte[] createMinimalDocx() {
        byte[] bytes = new byte[100];
        bytes[0] = 0x50;
        bytes[1] = 0x4B;
        bytes[2] = 0x03;
        bytes[3] = 0x04;
        return bytes;
    }
}
