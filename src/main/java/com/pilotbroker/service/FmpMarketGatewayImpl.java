package com.pilotbroker.service;

import com.pilotbroker.service.interfaces.FmpMarketGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FmpMarketGatewayImpl implements FmpMarketGateway {

    @Qualifier("fmpRestClient")
    private final RestClient fmpRestClient;

    @Value("${fmp.api.key}")
    private String apiKey;

    @Override
    public List<Map<String, Object>> fetchIndexQuotes() {
        try {
            Map[] response = fmpRestClient.get()
                    .uri("/quotes/index?apikey={key}", apiKey)
                    .retrieve()
                    .body(Map[].class);
            return toList(response);
        } catch (Exception e) {
            log.error("Erro FMP /quotes/index: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<Map<String, Object>> fetchActives() {
        try {
            Map[] response = fmpRestClient.get()
                    .uri("/actives?apikey={key}", apiKey)
                    .retrieve()
                    .body(Map[].class);
            return toList(response);
        } catch (Exception e) {
            log.error("Erro FMP /actives: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<Map<String, Object>> fetchEconomicCalendar(String from, String to) {
        try {
            Map[] response = fmpRestClient.get()
                    .uri("/economic-calendar?from={from}&to={to}&apikey={key}", from, to, apiKey)
                    .retrieve()
                    .body(Map[].class);
            return toList(response);
        } catch (Exception e) {
            log.error("Erro FMP /economic-calendar: {}", e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> toList(Map[] arr) {
        if (arr == null) return List.of();
        return Arrays.stream(arr).map(m -> (Map<String, Object>) m).toList();
    }
}
