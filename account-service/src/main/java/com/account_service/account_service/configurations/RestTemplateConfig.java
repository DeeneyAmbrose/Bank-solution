package com.account_service.account_service.configurations;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public GroupedOpenApi groupedOpenApi() {
        return  GroupedOpenApi.builder()
                .group("api")
                .packagesToScan("com.account_service.account_service")
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
