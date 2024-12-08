package rocks.m2x.demo.service.report;

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
import org.springframework.stereotype.Service;
import rocks.m2x.demo.config.ApplicationConfigurationProperties;
import rocks.m2x.demo.service.report.customhtmlrender.NicerListRenderer;
import rocks.m2x.demo.service.report.customhtmlrender.NicerListStyleType;
import rocks.m2x.demo.service.report.data.Control;
import rocks.m2x.demo.service.report.data.SoA;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class RenderDocxReportService {
    String DATE_FORMAT = "yyyy-MM-dd";

    final ApplicationConfigurationProperties config;

    public ByteArrayOutputStream renderSoa(SoA i) throws IOException {
        ApplicationConfigurationProperties.Export exportConfig = config.getExport();
        try (InputStream templateIs = exportConfig.getTemplate().getInputStream()) {
            i.setCreated(LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));

            // numbering controls by concatenating group nr and control nr
            i.getGroups().forEach(group -> {
                AtomicInteger cn = new AtomicInteger(1);
                List<Control> controls = group.getControls();
                if (controls != null) {
                    controls.forEach(c -> {
                        c.setNr(group.getNr() + "." + c.getNr());
                        cn.getAndIncrement();
                    });
                }
            });

            HtmlRenderConfig htmlRenderConfig = new HtmlRenderConfig();
            htmlRenderConfig.setNumberingSpacing(STLevelSuffix.Enum.forString("tab"));
            int listLeftAndIndent = 125;
            htmlRenderConfig.setNumberingHanging(listLeftAndIndent);
            htmlRenderConfig.setNumberingIndent(listLeftAndIndent);
            htmlRenderConfig.setCustomRenderers(List.of(new NicerListRenderer(NicerListStyleType.NicerUnordered.DISC).setLeft(listLeftAndIndent)));

            HtmlRenderPolicy htmlRenderPolicy = new HtmlRenderPolicy(htmlRenderConfig);
            Configure config = Configure.builder()
                    .useSpringEL(false)
                    .bind(htmlRenderPolicy, "description", "company")
                    .build();

            try (XWPFTemplate t = XWPFTemplate.compile(templateIs, config)) {
                XWPFTemplate d = t.render(i);
                NiceXWPFDocument xwpfDocument = d.getXWPFDocument();
                if (exportConfig.isReadonly()) {
                    // read only protection
                    xwpfDocument.enforceReadonlyProtection();
                }
                if (exportConfig.isEnforceUpdateFields()) {
                    // enforce update fields on next open.
                    xwpfDocument.enforceUpdateFields();
                }

                if (i.isDraft()) {
                    try {
                        addWatermatermark(xwpfDocument, "!!! DRAFT !!!");
                    } catch (InvalidFormatException e) {
                        throw new IOException(e);
                    }
                }

                log.debug("Rendering docx report for SoA {} done.", i.getVersion());

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                d.write(byteArrayOutputStream);

                return byteArrayOutputStream;
            }
        }
    }

    void addWatermatermark(XWPFDocument document, String text) throws InvalidFormatException {
        // Kopfzeile hinzufügen
        XWPFHeaderFooterPolicy headerFooterPolicy = document.getHeaderFooterPolicy();
        if (headerFooterPolicy == null) {
            headerFooterPolicy = document.createHeaderFooterPolicy();
        }

        // Kopfzeile der ersten Seite erstellen
        XWPFHeader firstPageHeader = headerFooterPolicy.getFirstPageHeader();
        if (firstPageHeader == null) {
            firstPageHeader = headerFooterPolicy.createHeader(XWPFHeaderFooterPolicy.FIRST);
        }
        createHeader(firstPageHeader, text);

        // Kopfzeile auf anderen Seiten erstellen
        XWPFHeader header = headerFooterPolicy.getDefaultHeader();
        if (header == null) {
            header = headerFooterPolicy.createHeader(XWPFHeaderFooterPolicy.DEFAULT);
        }
        createHeader(header, text);
    }

    private static void createHeader(XWPFHeader header, String text) {
        XWPFParagraph paragraph = header.getParagraphArray(0);
        if (paragraph == null) {
            paragraph = header.createParagraph();
        }
        paragraph.setStyle("IntensivesZitat"); // Verwende die Formatvorlage "IntensivesZitat"

        // Text in der Kopfzeile erstellen
        XWPFRun run = paragraph.createRun();
        run.setText(text);
    }

}
