package com.pilotbroker.service;

import com.pilotbroker.web.dto.stock.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private FmpStockGateway fmpGateway;

    @InjectMocks
    private StockService stockService;

    // ── search ──────────────────────────────────────────────────────────────

    @Test
    void searchStocks_DeveRetornarListaMapeada() {
        when(fmpGateway.fetchSearch("AAPL")).thenReturn(List.of(
            Map.of("symbol", "AAPL", "name", "Apple Inc.", "currency", "USD", "stockExchange", "NASDAQ")
        ));

        List<SearchResultDto> result = stockService.searchStocks("AAPL");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSymbol()).isEqualTo("AAPL");
        assertThat(result.get(0).getName()).isEqualTo("Apple Inc.");
        assertThat(result.get(0).getCurrency()).isEqualTo("USD");
        assertThat(result.get(0).getStockExchange()).isEqualTo("NASDAQ");
    }

    // ── screener ─────────────────────────────────────────────────────────────

    @Test
    void getScreener_DeveRepassarParamsERetornarListaMapeada() {
        Map<String, String> params = Map.of("sector", "Technology");
        when(fmpGateway.fetchScreener(params)).thenReturn(List.of(
            Map.of("symbol", "AAPL", "companyName", "Apple Inc.",
                   "price", 175.30, "changesPercentage", 0.71, "sector", "Technology")
        ));

        List<ScreenerResponseDto> result = stockService.getScreener(params);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSymbol()).isEqualTo("AAPL");
        assertThat(result.get(0).getChangePercent()).isEqualTo(0.71);
        assertThat(result.get(0).getSector()).isEqualTo("Technology");
    }

    // ── getStockDetail ────────────────────────────────────────────────────────

    @Test
    void getStockDetail_DeveRetornarDtoAgregado_QuandoGatewayResponde() {
        when(fmpGateway.fetchProfile("AAPL")).thenReturn(
            Map.of("symbol", "AAPL", "companyName", "Apple Inc.", "price", 175.30,
                   "image", "https://fmp.com/AAPL.png"));
        when(fmpGateway.fetchQuote("AAPL")).thenReturn(
            Map.of("price", 175.30, "change", 1.23, "changesPercentage", 0.71, "volume", 52000000L));
        when(fmpGateway.fetchChart("AAPL")).thenReturn(List.of(
            Map.of("date", "2026-03-27 09:30", "open", 174.0, "high", 176.0,
                   "low", 173.5, "close", 175.30, "volume", 1500000L)));
        when(fmpGateway.fetchPeers("AAPL")).thenReturn(List.of("MSFT", "GOOGL"));

        StockDetailDto result = stockService.getStockDetail("AAPL");

        assertThat(result).isNotNull();
        assertThat(result.getProfile().getSymbol()).isEqualTo("AAPL");
        assertThat(result.getProfile().getImage()).isEqualTo("https://fmp.com/AAPL.png");
        assertThat(result.getQuote().getChangePercent()).isEqualTo(0.71);
        assertThat(result.getCandlesticks()).hasSize(1);
        assertThat(result.getCandlesticks().get(0).getDate()).isEqualTo("2026-03-27 09:30");
        assertThat(result.getPeers()).containsExactly("MSFT", "GOOGL");
    }

    @Test
    void getStockDetail_DeveRetornarCandlesticksVazios_QuandoChartNull() {
        when(fmpGateway.fetchProfile("AAPL")).thenReturn(Map.of("symbol", "AAPL", "companyName", "", "price", 0.0, "image", ""));
        when(fmpGateway.fetchQuote("AAPL")).thenReturn(Map.of("price", 0.0, "change", 0.0, "changesPercentage", 0.0, "volume", 0L));
        when(fmpGateway.fetchChart("AAPL")).thenReturn(null);
        when(fmpGateway.fetchPeers("AAPL")).thenReturn(List.of());

        StockDetailDto result = stockService.getStockDetail("AAPL");

        assertThat(result.getCandlesticks()).isEmpty();
    }

    @Test
    void getStockDetail_DeveLancarException_QuandoGatewayFalha() {
        when(fmpGateway.fetchProfile(any())).thenThrow(new RuntimeException("FMP indisponível"));

        assertThatThrownBy(() -> stockService.getStockDetail("AAPL"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Erro ao buscar detalhes do ativo");
    }

    // ── fundamentals ─────────────────────────────────────────────────────────

    @Test
    void getFundamentals_DeveRetornarSubObjetosSeparados() {
        when(fmpGateway.fetchIncomeStatement("AAPL")).thenReturn(
            List.of(Map.of("date", "2025-12-31", "revenue", 124300000000L)));
        when(fmpGateway.fetchBalanceSheet("AAPL")).thenReturn(
            List.of(Map.of("date", "2025-12-31", "totalAssets", 352000000000L)));
        when(fmpGateway.fetchCashFlow("AAPL")).thenReturn(
            List.of(Map.of("date", "2025-12-31", "operatingCashFlow", 53800000000L)));
        when(fmpGateway.fetchRatios("AAPL")).thenReturn(
            Map.of("peRatio", 28.5, "roe", 1.47));
        when(fmpGateway.fetchScores("AAPL")).thenReturn(
            Map.of("altmanZScore", 4.2, "piotroskiScore", 7));

        FundamentalsDto result = stockService.getFundamentals("AAPL");

        assertThat(result.getIncomeStatement()).hasSize(1);
        assertThat(result.getBalanceSheet()).hasSize(1);
        assertThat(result.getCashFlow()).hasSize(1);
        assertThat(result.getRatios()).containsEntry("peRatio", 28.5);
        assertThat(result.getScores()).containsEntry("altmanZScore", 4.2);
    }

    // ── governance ────────────────────────────────────────────────────────────

    @Test
    void getGovernance_DeveRetornarSubObjetosSeparados() {
        when(fmpGateway.fetchExecutives("AAPL")).thenReturn(
            List.of(Map.of("name", "Tim Cook", "title", "CEO")));
        when(fmpGateway.fetchDividends("AAPL")).thenReturn(
            List.of(Map.of("date", "2026-02-10", "dividend", 0.25)));
        when(fmpGateway.fetchEarnings("AAPL")).thenReturn(
            List.of(Map.of("date", "2026-01-30", "eps", 2.18)));

        GovernanceDto result = stockService.getGovernance("AAPL");

        assertThat(result.getExecutives()).hasSize(1);
        assertThat(result.getExecutives().get(0)).containsEntry("name", "Tim Cook");
        assertThat(result.getDividends()).hasSize(1);
        assertThat(result.getEarnings()).hasSize(1);
    }
}
