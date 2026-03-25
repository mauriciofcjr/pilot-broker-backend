package com.pilotbroker.service;

import com.pilotbroker.web.dto.market.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketService {

    private final FmpMarketGateway fmpGateway;

    private static final Set<String> INDEX_SYMBOLS = Set.of("^GSPC", "^IXIC", "^DJI");
    private static final int MAX_ACTIVES = 10;
    private static final int MAX_CALENDAR_EVENTS = 20;

    @Cacheable("dashboard")
    public DashboardResponseDto getDashboard() {
        String from = LocalDate.now().toString();
        String to = LocalDate.now().plusDays(7).toString();

        return new DashboardResponseDto(
                mapIndices(fmpGateway.fetchIndexQuotes()),
                mapActives(fmpGateway.fetchActives()),
                mapCalendar(fmpGateway.fetchEconomicCalendar(from, to))
        );
    }

    private List<IndexQuoteDto> mapIndices(List<Map<String, Object>> raw) {
        if (raw == null) return List.of();
        return raw.stream()
                .filter(m -> INDEX_SYMBOLS.contains(str(m, "symbol")))
                .map(m -> new IndexQuoteDto(
                        str(m, "symbol"),
                        str(m, "name"),
                        toDouble(m.get("price")),
                        toDouble(m.get("change"))))
                .toList();
    }

    private List<ActivelyTradingDto> mapActives(List<Map<String, Object>> raw) {
        if (raw == null) return List.of();
        return raw.stream()
                .limit(MAX_ACTIVES)
                .map(m -> new ActivelyTradingDto(
                        str(m, "symbol"),
                        str(m, "name"),
                        toDouble(m.get("price")),
                        toDouble(m.get("change")),
                        toLong(m.get("volume"))))
                .toList();
    }

    private List<EconomicEventDto> mapCalendar(List<Map<String, Object>> raw) {
        if (raw == null) return List.of();
        return raw.stream()
                .limit(MAX_CALENDAR_EVENTS)
                .map(m -> new EconomicEventDto(
                        str(m, "event"),
                        str(m, "date"),
                        str(m, "impact")))
                .toList();
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : "";
    }

    private Double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }

    private Long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number n) return n.longValue();
        try { return Long.parseLong(val.toString()); } catch (Exception e) { return 0L; }
    }
}
