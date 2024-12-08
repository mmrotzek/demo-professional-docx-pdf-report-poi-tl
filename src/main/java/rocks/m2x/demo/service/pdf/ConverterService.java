package rocks.m2x.demo.service.pdf;

import rocks.m2x.demo.config.ApplicationConfigurationProperties;
import rocks.m2x.demo.service.exc.PdfConversionException;

import java.io.ByteArrayOutputStream;

public interface ConverterService {
    ByteArrayOutputStream convert(byte[] docxData, ApplicationConfigurationProperties.PdfConversionConfig config) throws PdfConversionException;
}
