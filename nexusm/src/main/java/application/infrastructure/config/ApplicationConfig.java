package application.infrastructure.config;

import application.domain.services.DomainService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * Registers the domain services as beans. The domain does not know Spring, so they do not
 * use {@code @Service}: they are created here.
 */
@Configuration
@ComponentScan(basePackages = "application.domain.services", useDefaultFilters = false,
    includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = DomainService.class))
public class ApplicationConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
