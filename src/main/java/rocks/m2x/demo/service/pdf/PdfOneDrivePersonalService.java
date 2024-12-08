package rocks.m2x.demo.service.pdf;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rocks.m2x.demo.config.ApplicationConfigurationProperties;
import rocks.m2x.demo.service.exc.PdfConversionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PdfOneDrivePersonalService implements ConverterService {
    @Override
    public ByteArrayOutputStream convert(byte[] docxData, ApplicationConfigurationProperties.PdfConversionConfig config) throws IOException, PdfConversionException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
