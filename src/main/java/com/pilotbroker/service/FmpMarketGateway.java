package com.pilotbroker.service;

import java.util.List;
import java.util.Map;

/**
 * Gateway para chamadas à API Financial Modeling Prep (FMP) — módulo Market.
 * Interface segregada para facilitar testes unitários do MarketService.
 */
public interface FmpMarketGateway {

    List<Map<String, Object>> fetchIndexQuotes();

    List<Map<String, Object>> fetchActives();

    List<Map<String, Object>> fetchEconomicCalendar(String from, String to);
}
