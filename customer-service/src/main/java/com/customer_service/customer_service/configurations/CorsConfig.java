package com.customer_service.customer_service.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.applyPermitDefaultValues();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:4200"));
        corsConfiguration.setAllowedMethods(Arrays.asList("GET","POST", "PATCH", "PUT", "DELETE", "OPTIONS"));

        corsConfiguration.setAllowedHeaders(Arrays.asList("Access-Control-Allow-Origin: *","Access-Control-Allow-Credentials:  Origin, Content-Type, X-Auth-Token, Authorization, Accept"));
        corsConfiguration.setExposedHeaders(List.of("X-Get-Header"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}
