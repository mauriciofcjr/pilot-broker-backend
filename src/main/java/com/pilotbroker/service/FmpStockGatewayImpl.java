package com.pilotbroker.service;

import com.pilotbroker.service.interfaces.FmpStockGateway;
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
public class FmpStockGatewayImpl implements FmpStockGateway {

    @Qualifier("fmpRestClient")
    private final RestClient fmpRestClient;

    @Value("${fmp.api.key}")
    private String apiKey;

    @Override
    public List<Map<String, Object>> fetchSearch(String query) {
        try {
            Map[] response = fmpRestClient.get()
                .uri("/search?query={q}&apikey={key}", query, apiKey)
                .retrieve()
                .body(Map[].class);
            return toList(response);
        } catch (Exception e) {
            log.error("Erro FMP /search: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<Map<String, Object>> fetchScreener(Map<String, String> params) {
        try {
            Map[] response = fmpRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/company-screener").queryParam("apikey", apiKey);
                    if (params != null) params.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .body(Map[].class);
            return toList(response);
        } catch (Exception e) {
            log.error("Erro FMP /company-screener: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public Map<String, Object> fetchProfile(String symbol) {
        try {
            Map[] response = fmpRestClient.get()
                .uri("/profile/{symbol}?apikey={key}", symbol, apiKey)
                .retrieve()
                .body(Map[].class);
            return firstOrEmpty(response);
        } catch (Exception e) {
            log.error("Erro FMP /profile/{}: {}", symbol, e.getMessage());
            return Map.of();
        }
    }

    @Override
    public Map<String, Object> fetchQuote(String symbol) {
        try {
            Map[] response = fmpRestClient.get()
                .uri("/quote/{symbol}?apikey={key}", symbol, apiKey)
                .retrieve()
                .body(Map[].class);
            return firstOrEmpty(response);
        } catch (Exception e) {
            log.error("Erro FMP /quote/{}: {}", symbol, e.getMessage());
            return Map.of();
        }
    }

    @Override
    public List<Map<String, Object>> fetchChart(String symbol) {
        try {
            Map[] response = fmpRestClient.get()
                .uri("/historical-chart/1min/{symbol}?apikey={key}", symbol, apiKey)
                .retrieve()
                .body(Map[].class);
            return toList(response);
        } catch (Exception e) {
            log.error("Erro FMP /historical-chart/{}: {}", symbol, e.getMessage());
            return List.of();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> fetchPeers(String symbol) {
        try {
            Map[] response = fmpRestClient.get()
                .uri("/stock-peers/{symbol}?apikey={key}", symbol, apiKey)
                .retrieve()
                .body(Map[].class);
            if (response == null || response.length == 0) return List.of();
            Object peersList = response[0].get("peersList");
            if (peersList instanceof List<?> list) {
                return list.stream().map(Object::toString).toList();
            }
            return List.of();
        } catch (Exception e) {
            log.error("Erro FMP /stock-peers/{}: {}", symbol, e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<Map<String, Object>> fetchIncomeStatement(String symbol) {
        return fetchList("/income-statement/{symbol}?apikey={key}", symbol);
    }

    @Override
    public List<Map<String, Object>> fetchBalanceSheet(String symbol) {
        return fetchList("/balance-sheet-statement/{symbol}?apikey={key}", symbol);
    }

    @Override
    public List<Map<String, Object>> fetchCashFlow(String symbol) {
        return fetchList("/cash-flow-statement/{symbol}?apikey={key}", symbol);
    }

    @Override
    public Map<String, Object> fetchRatios(String symbol) {
        try {
            Map[] response = fmpRestClient.get()
                .uri("/ratios/{symbol}?apikey={key}", symbol, apiKey)
                .retrieve()
                .body(Map[].class);
            return firstOrEmpty(response);
        } catch (Exception e) {
            log.error("Erro FMP /ratios/{}: {}", symbol, e.getMessage());
            return Map.of();
        }
    }

    @Override
    public Map<String, Object> fetchScores(String symbol) {
        try {
            Map[] response = fmpRestClient.get()
                .uri("/financial-scores/{symbol}?apikey={key}", symbol, apiKey)
                .retrieve()
                .body(Map[].class);
            return firstOrEmpty(response);
        } catch (Exception e) {
            log.error("Erro FMP /financial-scores/{}: {}", symbol, e.getMessage());
            return Map.of();
        }
    }

    @Override
    public List<Map<String, Object>> fetchExecutives(String symbol) {
        return fetchList("/key-executives/{symbol}?apikey={key}", symbol);
    }

    @Override
    public List<Map<String, Object>> fetchDividends(String symbol) {
        return fetchList("/dividends/{symbol}?apikey={key}", symbol);
    }

    @Override
    public List<Map<String, Object>> fetchEarnings(String symbol) {
        return fetchList("/earnings/{symbol}?apikey={key}", symbol);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private List<Map<String, Object>> fetchList(String uriTemplate, String symbol) {
        try {
            Map[] response = fmpRestClient.get()
                .uri(uriTemplate, symbol, apiKey)
                .retrieve()
                .body(Map[].class);
            return toList(response);
        } catch (Exception e) {
            log.error("Erro FMP {}: {}", uriTemplate, e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> firstOrEmpty(Map[] arr) {
        if (arr == null || arr.length == 0) return Map.of();
        return (Map<String, Object>) arr[0];
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> toList(Map[] arr) {
        if (arr == null) return List.of();
        return Arrays.stream(arr).map(m -> (Map<String, Object>) m).toList();
    }
}
