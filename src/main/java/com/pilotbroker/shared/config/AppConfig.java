package com.pilotbroker.shared.config;

import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.Locale;
import java.util.TimeZone;

@Configuration
@EnableCaching
public class AppConfig {

    @Value("${fmp.api.base-url}")
    private String fmpBaseUrl;

    @PostConstruct
    void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        Locale.setDefault(new Locale("pt", "BR"));
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean("fmpRestClient")
    public RestClient fmpRestClient() {
        return RestClient.builder()
                .baseUrl(fmpBaseUrl)
                .build();
    }
}
