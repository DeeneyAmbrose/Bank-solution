package com.customer_service.customer_service.configurations;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("Customer System").description("A system for managing Bank Customers").version("1.0.0"));
    }

    @Bean
    public GroupedOpenApi groupedOpenApi() {
        return  GroupedOpenApi.builder()
                    .group("api")
                    .packagesToScan("com.customer_service.customer_service")
                    .addOpenApiCustomizer(openApi -> {
                        openApi.getComponents()
                                .addSecuritySchemes(
                                        "bearer-token",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                );
                        openApi.addSecurityItem(new SecurityRequirement().addList("bearer-token"));
                    }).build();
    }
}
