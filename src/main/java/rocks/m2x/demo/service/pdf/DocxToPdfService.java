package rocks.m2x.demo.service.pdf;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rocks.m2x.demo.config.ApplicationConfigurationProperties;
import rocks.m2x.demo.service.exc.InvalidConfigurationException;
import rocks.m2x.demo.service.exc.PdfConversionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DocxToPdfService {
    final ApplicationConfigurationProperties config;
    final PdfLibreOfficeService pdfLibreOfficeService;
    final PdfOneDrivePersonalService pdfOneDrivePersonalService;

    public ByteArrayOutputStream convertDocxToPdf(byte[] docxData) throws InvalidConfigurationException, PdfConversionException, IOException {
        try {
            Objects.requireNonNull(config, "config is required");
            Objects.requireNonNull(config.getExport(), "config.export is required");
            Objects.requireNonNull(config.getExport().getPdfConversion(), "config.export.pdf-conversion is required");

            ApplicationConfigurationProperties.PdfConversionConfig pdfConversion = config.getExport().getPdfConversion();
            if (pdfConversion.getPdfConversion() == ApplicationConfigurationProperties.PdfConversionConfig.PdfConverter.LIBREOFFICE) {
                return pdfLibreOfficeService.convert(docxData, config.getExport().getPdfConversion());
            } else if (pdfConversion.getPdfConversion() == ApplicationConfigurationProperties.PdfConversionConfig.PdfConverter.GRAPH_API) {
                return pdfOneDrivePersonalService.convert(docxData, config.getExport().getPdfConversion());
            } else {
                throw new InvalidConfigurationException("Unknown pdf conversion method " + pdfConversion.getPdfConversion());
            }

        } catch (NullPointerException e) {
            throw new InvalidConfigurationException("docx2pdf conversion - Configuration is missing required properties. " + e.getMessage(), e);
        }
    }


}
