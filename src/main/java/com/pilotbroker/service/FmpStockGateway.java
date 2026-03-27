package com.pilotbroker.service;

import java.util.List;
import java.util.Map;

/**
 * Gateway para chamadas à API Financial Modeling Prep (FMP) — módulo Stock.
 * Interface segregada para facilitar testes unitários do StockService.
 */
public interface FmpStockGateway {

    List<Map<String, Object>> fetchSearch(String query);

    List<Map<String, Object>> fetchScreener(Map<String, String> params);

    Map<String, Object> fetchProfile(String symbol);

    Map<String, Object> fetchQuote(String symbol);

    List<Map<String, Object>> fetchChart(String symbol);

    List<String> fetchPeers(String symbol);

    List<Map<String, Object>> fetchIncomeStatement(String symbol);

    List<Map<String, Object>> fetchBalanceSheet(String symbol);

    List<Map<String, Object>> fetchCashFlow(String symbol);

    Map<String, Object> fetchRatios(String symbol);

    Map<String, Object> fetchScores(String symbol);

    List<Map<String, Object>> fetchExecutives(String symbol);

    List<Map<String, Object>> fetchDividends(String symbol);

    List<Map<String, Object>> fetchEarnings(String symbol);
}
