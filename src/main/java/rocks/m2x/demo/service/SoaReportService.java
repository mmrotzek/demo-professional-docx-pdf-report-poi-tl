package rocks.m2x.demo.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import rocks.m2x.demo.service.data.SoA;
import rocks.m2x.demo.service.exc.InvalidConfigurationException;
import rocks.m2x.demo.service.exc.PdfConversionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SoaReportService {
    final RenderDocxService renderService;
    final DocxToPdfService docxToPdfService;

    public Pair<SoA, ByteArrayOutputStream> renderReport() throws IOException {
        SoA demoData = getDemoData();
        return Pair.of(demoData, renderService.renderSoa(demoData));
    }

    public Pair<SoA, ByteArrayOutputStream> renderReportPdf() throws PdfConversionException, IOException, InvalidConfigurationException {
        SoA demoData = getDemoData();
        try (ByteArrayOutputStream docx = renderService.renderSoa(demoData)) {
            return Pair.of(demoData, docxToPdfService.convertDocxToPdf(docx.toByteArray()));
        }
    }

    SoA getDemoData() {
        return new SoA();
    }
}
