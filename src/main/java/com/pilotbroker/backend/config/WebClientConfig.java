package com.pilotbroker.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${fmp.api.base-url}")
    private String fmpBaseUrl;

    @Value("${fmp.api.key}")
    private String fmpApiKey;

    @Bean
    public WebClient fmpWebClient() {
        return WebClient.builder()
                .baseUrl(fmpBaseUrl)
                .defaultUriVariables(java.util.Map.of("apikey", fmpApiKey))
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
