package com.pilotbroker.service;

import com.pilotbroker.web.dto.stock.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final FmpStockGateway fmpGateway;

    // ── search ────────────────────────────────────────────────────────────────

    @Cacheable("search")
    public List<SearchResultDto> searchStocks(String query) {
        List<Map<String, Object>> raw = fmpGateway.fetchSearch(query);
        if (raw == null) return List.of();
        return raw.stream()
            .map(m -> new SearchResultDto(
                str(m, "symbol"), str(m, "name"),
                str(m, "currency"), str(m, "stockExchange")))
            .toList();
    }

    // ── screener ──────────────────────────────────────────────────────────────

    @Cacheable("screener")
    public List<ScreenerResponseDto> getScreener(Map<String, String> params) {
        List<Map<String, Object>> raw = fmpGateway.fetchScreener(params);
        if (raw == null) return List.of();
        return raw.stream()
            .map(m -> new ScreenerResponseDto(
                str(m, "symbol"), str(m, "companyName"),
                toDouble(m.get("price")), toDouble(m.get("changesPercentage")),
                str(m, "sector")))
            .toList();
    }

    // ── stock detail (Virtual Threads) ────────────────────────────────────────

    @Cacheable("stockDetail")
    public StockDetailDto getStockDetail(String symbol) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var profileFuture = executor.submit(() -> fmpGateway.fetchProfile(symbol));
            var quoteFuture   = executor.submit(() -> fmpGateway.fetchQuote(symbol));
            var chartFuture   = executor.submit(() -> fmpGateway.fetchChart(symbol));
            var peersFuture   = executor.submit(() -> fmpGateway.fetchPeers(symbol));

            return new StockDetailDto(
                mapProfile(profileFuture.get()),
                mapQuote(quoteFuture.get()),
                mapCandlesticks(chartFuture.get()),
                nullSafeStringList(peersFuture.get())
            );
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao buscar detalhes do ativo: " + symbol, e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operação interrompida ao buscar ativo: " + symbol, e);
        }
    }

    // ── fundamentals (Virtual Threads) ────────────────────────────────────────

    @Cacheable("fundamentals")
    public FundamentalsDto getFundamentals(String symbol) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var incomeFuture  = executor.submit(() -> fmpGateway.fetchIncomeStatement(symbol));
            var balanceFuture = executor.submit(() -> fmpGateway.fetchBalanceSheet(symbol));
            var cashFuture    = executor.submit(() -> fmpGateway.fetchCashFlow(symbol));
            var ratiosFuture  = executor.submit(() -> fmpGateway.fetchRatios(symbol));
            var scoresFuture  = executor.submit(() -> fmpGateway.fetchScores(symbol));

            var ratios = ratiosFuture.get();
            var scores = scoresFuture.get();
            return new FundamentalsDto(
                nullSafeList(incomeFuture.get()),
                nullSafeList(balanceFuture.get()),
                nullSafeList(cashFuture.get()),
                ratios != null ? ratios : Map.of(),
                scores != null ? scores : Map.of()
            );
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao buscar fundamentals: " + symbol, e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operação interrompida ao buscar fundamentals: " + symbol, e);
        }
    }

    // ── governance (Virtual Threads) ──────────────────────────────────────────

    @Cacheable("governance")
    public GovernanceDto getGovernance(String symbol) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var execFuture     = executor.submit(() -> fmpGateway.fetchExecutives(symbol));
            var divFuture      = executor.submit(() -> fmpGateway.fetchDividends(symbol));
            var earningsFuture = executor.submit(() -> fmpGateway.fetchEarnings(symbol));

            return new GovernanceDto(
                nullSafeList(execFuture.get()),
                nullSafeList(divFuture.get()),
                nullSafeList(earningsFuture.get())
            );
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao buscar governance: " + symbol, e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operação interrompida ao buscar governance: " + symbol, e);
        }
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private ProfileDto mapProfile(Map<String, Object> m) {
        if (m == null) return new ProfileDto("", "", 0.0, "");
        return new ProfileDto(
            str(m, "symbol"), str(m, "companyName"),
            toDouble(m.get("price")), str(m, "image"));
    }

    private QuoteDto mapQuote(Map<String, Object> m) {
        if (m == null) return new QuoteDto(0.0, 0.0, 0.0, 0L);
        return new QuoteDto(
            toDouble(m.get("price")),
            toDouble(m.get("change")),
            toDouble(m.get("changesPercentage")),
            toLong(m.get("volume")));
    }

    private List<CandlestickDto> mapCandlesticks(List<Map<String, Object>> raw) {
        if (raw == null) return List.of();
        return raw.stream()
            .map(m -> new CandlestickDto(
                str(m, "date"),
                toDouble(m.get("open")), toDouble(m.get("high")),
                toDouble(m.get("low")),  toDouble(m.get("close")),
                toLong(m.get("volume"))))
            .toList();
    }

    private List<Map<String, Object>> nullSafeList(List<Map<String, Object>> list) {
        return list != null ? list : List.of();
    }

    private List<String> nullSafeStringList(List<String> list) {
        return list != null ? list : List.of();
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
