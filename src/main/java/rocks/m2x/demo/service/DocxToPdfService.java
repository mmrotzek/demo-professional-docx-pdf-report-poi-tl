package rocks.m2x.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import rocks.m2x.demo.config.ApplicationConfigurationProperties;
import rocks.m2x.demo.service.exc.InvalidConfigurationException;
import rocks.m2x.demo.service.exc.PdfConversionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DocxToPdfService {
    private final RestTemplate restTemplate = new RestTemplate();
    final ApplicationConfigurationProperties config;

    public ByteArrayOutputStream convertDocxToPdf(byte[] docxData) throws InvalidConfigurationException, PdfConversionException, IOException {
        try {
            Objects.requireNonNull(config, "config is required");
            Objects.requireNonNull(config.getExport(), "config.export is required");
            Objects.requireNonNull(config.getExport().getPdfConversion(), "config.export.pdfConversion is required");

            ApplicationConfigurationProperties.PdfConversionConfig pdfConversion = config.getExport().getPdfConversion();
            if (pdfConversion.getPdfConversion() == ApplicationConfigurationProperties.PdfConversionConfig.PdfConverter.LIBREOFFICE) {
                Objects.requireNonNull(config.getExport().getPdfConversion().getLibreOffice(), "config.export.pdfConversion.libreOffice is required");
                return convertUsingLibreoffice(pdfConversion.getLibreOffice().getUrl(), docxData);
            } else if (pdfConversion.getPdfConversion() == ApplicationConfigurationProperties.PdfConversionConfig.PdfConverter.GRAPH_API) {
                throw new UnsupportedOperationException("Graph API conversion is not supported yet");
            } else {
                throw new InvalidConfigurationException("Unknown pdf conversion method " + pdfConversion.getPdfConversion());
            }

        } catch (NullPointerException e) {
            throw new InvalidConfigurationException("docx2pdf conversion - Configuration is missing required properties. " + e.getMessage(), e);
        }
    }


    protected ByteArrayOutputStream convertUsingLibreoffice(String url, byte[] docxData) throws IOException, PdfConversionException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // multipart/form-data headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // ByteArrayResource as file wrapper
            ByteArrayResource resource = new ByteArrayResource(docxData) {
                @Override
                public String getFilename() {
                    return "document.docx"; // filename of the form posted to the libreoffice api
                }
            };

            // create Multipart-Body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("document", resource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // call libreoffice API
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                out.write(Objects.requireNonNull(response.getBody()));
                return out;
            } else {
                throw new PdfConversionException("Error converting file to pdf: " + response.getStatusCode());
            }
        }
    }
}
