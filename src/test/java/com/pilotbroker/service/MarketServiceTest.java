package com.pilotbroker.service;

import com.pilotbroker.web.dto.market.DashboardResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketServiceTest {

    @Mock
    private FmpMarketGateway fmpGateway;

    @InjectMocks
    private MarketService marketService;

    @Test
    void getDashboard_DeveRetornarTresListas_QuandoGatewayResponde() {
        when(fmpGateway.fetchIndexQuotes()).thenReturn(List.of(
                Map.of("symbol", "^GSPC", "name", "S&P 500", "price", 5000.0, "change", 1.5)
        ));
        when(fmpGateway.fetchActives()).thenReturn(List.of(
                Map.of("symbol", "AAPL", "name", "Apple Inc.", "price", 189.0, "change", 2.0, "volume", 80000000L)
        ));
        when(fmpGateway.fetchEconomicCalendar(anyString(), anyString())).thenReturn(List.of(
                Map.of("event", "CPI Data", "date", "2026-03-25", "impact", "High")
        ));

        DashboardResponseDto result = marketService.getDashboard();

        assertThat(result).isNotNull();
        assertThat(result.getIndices()).hasSize(1);
        assertThat(result.getActivelyTrading()).hasSize(1);
        assertThat(result.getEconomicCalendar()).hasSize(1);
    }

    @Test
    void getDashboard_IndicesDeveFiltrarApenasSimbolosConhecidos() {
        when(fmpGateway.fetchIndexQuotes()).thenReturn(List.of(
                Map.of("symbol", "^GSPC", "name", "S&P 500", "price", 5000.0, "change", 1.0),
                Map.of("symbol", "^IXIC", "name", "NASDAQ", "price", 17000.0, "change", 0.5),
                Map.of("symbol", "^DJI", "name", "Dow Jones", "price", 39000.0, "change", -0.3),
                Map.of("symbol", "^RUT", "name", "Russell 2000", "price", 2000.0, "change", 0.1)
        ));
        when(fmpGateway.fetchActives()).thenReturn(List.of());
        when(fmpGateway.fetchEconomicCalendar(anyString(), anyString())).thenReturn(List.of());

        DashboardResponseDto result = marketService.getDashboard();

        assertThat(result.getIndices()).hasSize(3);
        assertThat(result.getIndices())
                .extracting("symbol")
                .containsExactlyInAnyOrder("^GSPC", "^IXIC", "^DJI");
    }

    @Test
    void getDashboard_AtivosDeveRespeitar10Limite() {
        List<Map<String, Object>> maisde10 = List.of(
                Map.of("symbol", "AAPL", "name", "Apple", "price", 100.0, "change", 1.0, "volume", 1000000L),
                Map.of("symbol", "MSFT", "name", "Microsoft", "price", 200.0, "change", 1.0, "volume", 1000000L),
                Map.of("symbol", "GOOGL", "name", "Alphabet", "price", 150.0, "change", 1.0, "volume", 1000000L),
                Map.of("symbol", "AMZN", "name", "Amazon", "price", 180.0, "change", 1.0, "volume", 1000000L),
                Map.of("symbol", "META", "name", "Meta", "price", 120.0, "change", 1.0, "volume", 1000000L),
                Map.of("symbol", "TSLA", "name", "Tesla", "price", 250.0, "change", 1.0, "volume", 1000000L),
                Map.of("symbol", "NVDA", "name", "NVIDIA", "price", 800.0, "change", 1.0, "volume", 1000000L),
                Map.of("symbol", "BRK.B", "name", "Berkshire", "price", 350.0, "change", 1.0, "volume", 1000000L),
                Map.of("symbol", "JPM", "name", "JPMorgan", "price", 200.0, "change", 1.0, "volume", 1000000L),
                Map.of("symbol", "V", "name", "Visa", "price", 270.0, "change", 1.0, "volume", 1000000L),
                Map.of("symbol", "MA", "name", "Mastercard", "price", 460.0, "change", 1.0, "volume", 1000000L)
        );

        when(fmpGateway.fetchIndexQuotes()).thenReturn(List.of());
        when(fmpGateway.fetchActives()).thenReturn(maisde10);
        when(fmpGateway.fetchEconomicCalendar(anyString(), anyString())).thenReturn(List.of());

        DashboardResponseDto result = marketService.getDashboard();

        assertThat(result.getActivelyTrading()).hasSize(10);
    }

    @Test
    void getDashboard_DeveRetornarListasVazias_QuandoGatewayRetornaNull() {
        when(fmpGateway.fetchIndexQuotes()).thenReturn(null);
        when(fmpGateway.fetchActives()).thenReturn(null);
        when(fmpGateway.fetchEconomicCalendar(anyString(), anyString())).thenReturn(null);

        DashboardResponseDto result = marketService.getDashboard();

        assertThat(result).isNotNull();
        assertThat(result.getIndices()).isEmpty();
        assertThat(result.getActivelyTrading()).isEmpty();
        assertThat(result.getEconomicCalendar()).isEmpty();
    }

    @Test
    void getDashboard_IndicesDeveMappearCamposCorretamente() {
        when(fmpGateway.fetchIndexQuotes()).thenReturn(List.of(
                Map.of("symbol", "^GSPC", "name", "S&P 500", "price", 5123.41, "change", 1.23)
        ));
        when(fmpGateway.fetchActives()).thenReturn(List.of());
        when(fmpGateway.fetchEconomicCalendar(anyString(), anyString())).thenReturn(List.of());

        DashboardResponseDto result = marketService.getDashboard();

        var index = result.getIndices().get(0);
        assertThat(index.getSymbol()).isEqualTo("^GSPC");
        assertThat(index.getName()).isEqualTo("S&P 500");
        assertThat(index.getPrice()).isEqualTo(5123.41);
        assertThat(index.getChange()).isEqualTo(1.23);
    }
}
