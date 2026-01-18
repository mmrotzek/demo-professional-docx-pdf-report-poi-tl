package rocks.m2x.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.time.Duration;

/**
 * Health indicator for PDF conversion service (LibreOffice/docx2pdf).
 * Checks if the PDF conversion service is reachable.
 */
@Component
@Slf4j
public class PdfConversionHealthIndicator implements HealthIndicator {

    private final ApplicationConfigurationProperties config;
    private final RestTemplate restTemplate;

    public PdfConversionHealthIndicator(ApplicationConfigurationProperties config) {
        this.config = config;
        // Create RestTemplate with timeout for health checks
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(5).toMillis());
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public Health health() {
        try {
            ApplicationConfigurationProperties.PdfConversionConfig pdfConversion = config.getExport().getPdfConversion();
            
            // Only check health if LibreOffice is configured
            if (pdfConversion == null || 
                pdfConversion.getPdfConversion() != ApplicationConfigurationProperties.PdfConversionConfig.PdfConverter.LIBREOFFICE ||
                pdfConversion.getLibreOffice() == null) {
                return Health.unknown()
                    .withDetail("reason", "PDF conversion not configured or not using LibreOffice")
                    .build();
            }

            String url = pdfConversion.getLibreOffice().getUrl();
            
            // Try to connect to the service - use a simple connectivity check
            // We'll attempt a connection and catch connection errors
            try {
                // Extract base URL (remove /pdf endpoint)
                String baseUrl = url.substring(0, url.lastIndexOf('/'));
                
                // Try a simple GET request to check connectivity
                // Use a short timeout to avoid blocking
                try {
                    restTemplate.getForObject(baseUrl, String.class);
                } catch (ResourceAccessException e) {
                    // Re-throw to handle in outer catch
                    throw e;
                } catch (org.springframework.web.client.HttpClientErrorException | 
                         org.springframework.web.client.HttpServerErrorException e) {
                    // HTTP errors (4xx/5xx) mean the service is up but endpoint doesn't exist
                    // That's fine - the service is reachable
                    log.debug("Service responded with HTTP error - service is reachable", e);
                }
                
                return Health.up()
                    .withDetail("service", "PDF Conversion Service (LibreOffice)")
                    .withDetail("url", url)
                    .withDetail("status", "Available")
                    .build();
            } catch (ResourceAccessException e) {
                Throwable cause = e.getCause();
                if (cause instanceof ConnectException) {
                    return Health.down()
                        .withDetail("service", "PDF Conversion Service (LibreOffice)")
                        .withDetail("url", url)
                        .withDetail("status", "Connection refused")
                        .withDetail("error", "Service is not reachable. Please start the docx2pdf service using: docker-compose up -d docx2pdf")
                        .withException(e)
                        .build();
                }
                // Other connection errors
                return Health.down()
                    .withDetail("service", "PDF Conversion Service (LibreOffice)")
                    .withDetail("url", url)
                    .withDetail("status", "Connection error")
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
            } catch (Exception e) {
                // Unexpected errors - log but don't fail health check
                log.warn("Unexpected error during PDF service health check", e);
                return Health.down()
                    .withDetail("service", "PDF Conversion Service (LibreOffice)")
                    .withDetail("url", url)
                    .withDetail("status", "Health check failed")
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
            }
        } catch (Exception e) {
            log.error("Error checking PDF conversion service health", e);
            return Health.down()
                .withDetail("service", "PDF Conversion Service (LibreOffice)")
                .withDetail("status", "Health check failed")
                .withDetail("error", e.getMessage())
                .withException(e)
                .build();
        }
    }
}
