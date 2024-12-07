package rocks.m2x.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@Slf4j
public class SecurityConfig {

    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource(ApplicationConfigurationProperties appConfigurationProperties) {
        CorsConfiguration configuration = new CorsConfiguration();

        if (appConfigurationProperties.getCors() != null) {
            String[] allowedOrigins = appConfigurationProperties.getCors().getAllowedOrigins();
            if (allowedOrigins != null && allowedOrigins.length > 0) {
                log.info("Register allowedOrigins: {}", Arrays.asList(allowedOrigins));
                configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
                configuration.setAllowCredentials(true);
                configuration.setAllowedMethods(Collections.singletonList(CorsConfiguration.ALL));
                configuration.setAllowedHeaders(Collections.singletonList(CorsConfiguration.ALL));
                configuration.setExposedHeaders(List.of("*"));
            }
        }
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
