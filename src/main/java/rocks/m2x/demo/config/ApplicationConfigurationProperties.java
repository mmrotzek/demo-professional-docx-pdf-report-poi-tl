package rocks.m2x.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "m2x.demo")
@Getter
@Setter
public class ApplicationConfigurationProperties {

    CorsConfig cors = new CorsConfig();
    ExportConfig export = new ExportConfig();

    @Getter
    @Setter
    public static class CorsConfig {
        String[] allowedOrigins = new String[0];
    }

    @Getter
    @Setter
    public static class ExportConfig {
        PdfConversionConfig pdfConversion = new PdfConversionConfig();
    }

    @Getter
    @Setter
    public static class PdfConversionConfig {
        PdfConverter pdfConversion = PdfConverter.LIBREOFFICE;
        PdfConversionLibreOfficeConfig libreOffice = new PdfConversionLibreOfficeConfig();

       public enum PdfConverter {
            LIBREOFFICE
        }

        @Getter
        @Setter
        public static class PdfConversionLibreOfficeConfig {
            String url = "http://localhost:7700/pdf";
        }
    }
}
