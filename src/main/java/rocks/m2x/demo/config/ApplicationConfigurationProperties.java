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
    Export export = new Export();

    @Getter
    @Setter
    public static class CorsConfig {
        String[] allowedOrigins = new String[0];
    }

    @Getter
    @Setter
    public static class Export {
        Resource template = new DefaultResourceLoader().getResource("classpath:templates/template.docx");
        boolean readonly = true;
        boolean enforceUpdateFields = false;

        PdfConversionConfig pdfConversion = new PdfConversionConfig();
    }


    @Getter
    @Setter
    public static class PdfConversionConfig {
        PdfConverter pdfConversion = PdfConverter.LIBREOFFICE;
        PdfConversionLibreOfficeConfig libreOffice = new PdfConversionLibreOfficeConfig();
        PdfConversionGraphApiConfig graphApi = new PdfConversionGraphApiConfig();

       public enum PdfConverter {
            LIBREOFFICE,
            GRAPH_API
        }

        @Getter
        @Setter
        public static class PdfConversionLibreOfficeConfig {
            String url = "http://localhost:7700/pdf";
        }

        @Getter
        @Setter
        public static class PdfConversionGraphApiConfig {
            String url = "";
        }
    }


}
