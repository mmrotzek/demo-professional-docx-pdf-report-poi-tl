package rocks.m2x.demo.service.mcp;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.xwpf.NiceXWPFDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.ddr.poi.html.HtmlRenderConfig;
import org.ddr.poi.html.HtmlRenderPolicy;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STLevelSuffix;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import rocks.m2x.demo.Constants;
import rocks.m2x.demo.mcp.model.ReportOptions;
import rocks.m2x.demo.service.report.customhtmlrender.NicerListRenderer;
import rocks.m2x.demo.service.report.customhtmlrender.NicerListStyleType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generic service for rendering DOCX reports from templates and dynamic data structures
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GenericReportService {

    /**
     * Render a DOCX report from a template and data
     *
     * @param templateResource Template resource
     * @param data Data to render (Map structure)
     * @param options Report generation options
     * @return ByteArrayOutputStream containing the rendered DOCX document
     * @throws IOException if rendering fails
     */
    public ByteArrayOutputStream renderReport(Resource templateResource, Map<String, Object> data, ReportOptions options)
            throws IOException {
        if (options == null) {
            options = ReportOptions.builder().build();
        }

        try (InputStream templateIs = templateResource.getInputStream()) {
            // Configure HTML rendering for specified fields
            Configure config = configureRendering(options);

            // Compile and render template
            try (XWPFTemplate template = XWPFTemplate.compile(templateIs, config)) {
                XWPFTemplate rendered = template.render(data);
                NiceXWPFDocument xwpfDocument = rendered.getXWPFDocument();

                // Apply document protection and options
                applyDocumentOptions(xwpfDocument, options);

                log.debug("Rendering generic docx report done.");

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                rendered.write(byteArrayOutputStream);

                return byteArrayOutputStream;
            }
        }
    }

    /**
     * Configure rendering based on options
     *
     * @param options Report options
     * @return Configure object for poi-tl
     */
    private Configure configureRendering(ReportOptions options) {
        var builder = Configure.builder()
                .useSpringEL(false);

        // Configure HTML rendering for specified fields
        Set<String> htmlFields = parseHtmlFields(options.getHtmlFields());
        if (!htmlFields.isEmpty()) {
            HtmlRenderConfig htmlRenderConfig = new HtmlRenderConfig();
            htmlRenderConfig.setNumberingSpacing(STLevelSuffix.Enum.forString("tab"));
            int listLeftAndIndent = 125;
            htmlRenderConfig.setNumberingHanging(listLeftAndIndent);
            htmlRenderConfig.setNumberingIndent(listLeftAndIndent);
            htmlRenderConfig.setCustomRenderers(List.of(
                    new NicerListRenderer(NicerListStyleType.NicerUnordered.DISC).setLeft(listLeftAndIndent)));

            HtmlRenderPolicy htmlRenderPolicy = new HtmlRenderPolicy(htmlRenderConfig);
            String[] htmlFieldArray = htmlFields.toArray(new String[0]);
            builder.bind(htmlRenderPolicy, htmlFieldArray);
        }

        return builder.build();
    }

    /**
     * Parse comma-separated HTML field names
     *
     * @param htmlFields Comma-separated field names
     * @return Set of field names
     */
    private Set<String> parseHtmlFields(String htmlFields) {
        if (htmlFields == null || htmlFields.trim().isEmpty()) {
            return Set.of();
        }
        return Arrays.stream(htmlFields.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Apply document options (readonly, draft watermark, etc.)
     *
     * @param document XWPFDocument
     * @param options Report options
     * @throws IOException if watermark application fails
     */
    private void applyDocumentOptions(NiceXWPFDocument document, ReportOptions options) throws IOException {
        if (options.getReadonly() != null && options.getReadonly()) {
            // Apply read-only protection
            document.enforceReadonlyProtection();
        }

        if (options.getEnforceUpdateFields() != null && options.getEnforceUpdateFields()) {
            // Enforce field updates on next open
            document.enforceUpdateFields();
        }

        if (options.getDraft() != null && options.getDraft()) {
            try {
                addWatermark(document, Constants.WATERMARK_DRAFT);
            } catch (InvalidFormatException e) {
                throw new IOException("Failed to add draft watermark", e);
            }
        }
    }

    /**
     * Add watermark to document
     *
     * @param document XWPFDocument
     * @param text Watermark text
     * @throws InvalidFormatException if watermark creation fails
     */
    private void addWatermark(XWPFDocument document, String text) throws InvalidFormatException {
        // Create header footer policy
        XWPFHeaderFooterPolicy headerFooterPolicy = document.getHeaderFooterPolicy();
        if (headerFooterPolicy == null) {
            headerFooterPolicy = document.createHeaderFooterPolicy();
        }

        // Create header for first page
        XWPFHeader firstPageHeader = headerFooterPolicy.getFirstPageHeader();
        if (firstPageHeader == null) {
            firstPageHeader = headerFooterPolicy.createHeader(XWPFHeaderFooterPolicy.FIRST);
        }
        createHeader(firstPageHeader, text);

        // Create header for other pages
        XWPFHeader header = headerFooterPolicy.getDefaultHeader();
        if (header == null) {
            header = headerFooterPolicy.createHeader(XWPFHeaderFooterPolicy.DEFAULT);
        }
        createHeader(header, text);
    }

    /**
     * Create header with watermark text
     *
     * @param header XWPFHeader
     * @param text Watermark text
     */
    private static void createHeader(XWPFHeader header, String text) {
        XWPFParagraph paragraph = header.getParagraphArray(0);
        if (paragraph == null) {
            paragraph = header.createParagraph();
        }
        paragraph.setStyle("IntensivesZitat");

        // Create run with watermark text
        XWPFRun run = paragraph.createRun();
        run.setText(text);
    }
}
