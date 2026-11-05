package application.infrastructure.config;

import application.adapters.in.rest.documentation.ApiDocumentation;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.RefundId;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.ShipmentId;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.List;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI description of the REST API: title, Bearer JWT scheme and one group per input port.
 */
@Configuration
public class OpenApiConfig {

    private static final String API_PREFIX = "/api/v1/";

    static {
        // The ids of the URLs are value objects, but they travel as text.
        for (Class<?> id : List.of(OrderId.class, ProductId.class, RefundId.class, ReturnId.class,
                ShipmentId.class, UserId.class, WarehouseId.class)) {
            SpringDocUtils.getConfig().replaceWithClass(id, String.class);
        }
    }

    @Bean
    public OpenAPI nexusMarketOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("NexusMarket API")
                .description("REST API of the NexusMarket marketplace. Log in with "
                    + "POST /api/v1/public/login and paste the token in the Authorize button. "
                    + "Each route prefix belongs to one role.")
                .version("1.0"))
            .components(new Components().addSecuritySchemes(ApiDocumentation.BEARER_AUTH,
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi publicGroup() {
        return group("public");
    }

    @Bean
    public GroupedOpenApi accountGroup() {
        return group("account");
    }

    @Bean
    public GroupedOpenApi buyerGroup() {
        return group("buyer");
    }

    @Bean
    public GroupedOpenApi sellerGroup() {
        return group("seller");
    }

    @Bean
    public GroupedOpenApi logisticsGroup() {
        return group("logistics");
    }

    @Bean
    public GroupedOpenApi adminGroup() {
        return group("admin");
    }

    @Bean
    public GroupedOpenApi supervisorGroup() {
        return group("supervisor");
    }

    private static GroupedOpenApi group(String name) {
        return GroupedOpenApi.builder()
            .group(name)
            .pathsToMatch(API_PREFIX + name + "/**")
            .build();
    }
}
