package rocks.m2x.demo.service.pdf;


import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import rocks.m2x.demo.config.ApplicationConfigurationProperties;
import rocks.m2x.demo.service.exc.PdfConversionException;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PdfLibreOfficeService implements ConverterService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public ByteArrayOutputStream convert(byte[] docxData, ApplicationConfigurationProperties.PdfConversionConfig config) throws PdfConversionException {
        Objects.requireNonNull(config.getLibreOffice(), "config.export.pdf-conversion.libre-office is required");
        String url = config.getLibreOffice().getUrl();

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
        } catch (Exception e) {
            throw new PdfConversionException("Error converting file to pdf", e);
        }
    }
}
