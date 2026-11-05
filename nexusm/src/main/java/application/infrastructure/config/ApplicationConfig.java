package application.infrastructure.config;

import application.domain.ports.out.InventoryRepository;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.DomainService;
import application.domain.services.InventoryDomainService;
import application.domain.services.PasswordPolicy;
import application.domain.services.WarehouseAllocationService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * Registra como beans los servicios de dominio. El dominio no conoce Spring, por eso no
 * llevan {@code @Service}: se crean aquí.
 */
@Configuration
@ComponentScan(basePackages = "application.domain.services", useDefaultFilters = false,
    includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = DomainService.class))
public class ApplicationConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public PasswordPolicy passwordPolicy() {
        return new PasswordPolicy();
    }

    @Bean
    public InventoryDomainService inventoryDomainService(InventoryRepository inventoryRepository) {
        return new InventoryDomainService(inventoryRepository);
    }

    @Bean
    public WarehouseAllocationService warehouseAllocationService(
            InventoryRepository inventoryRepository, WarehouseRepository warehouseRepository) {
        return new WarehouseAllocationService(inventoryRepository, warehouseRepository);
    }

}
