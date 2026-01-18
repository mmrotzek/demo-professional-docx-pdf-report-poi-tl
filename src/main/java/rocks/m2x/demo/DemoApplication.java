package rocks.m2x.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import rocks.m2x.demo.config.ApplicationConfigurationProperties;
import rocks.m2x.demo.config.McpProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationConfigurationProperties.class, McpProperties.class})
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
