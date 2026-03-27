package com.pilotbroker.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pilotbroker.model.Usuario;
import com.pilotbroker.repository.UsuarioRepository;
import com.pilotbroker.service.StockService;
import com.pilotbroker.web.dto.login.LoginRequestDto;
import com.pilotbroker.web.dto.stock.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class StockControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @MockBean
    private StockService stockService;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        usuarioRepository.deleteAll();

        Usuario user = new Usuario();
        user.setUsername("stock-it@email.com");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole(Usuario.Role.ROLE_CLIENTE);
        usuarioRepository.save(user);

        token = obterToken("stock-it@email.com", "123456");

        // stubs padrão
        when(stockService.searchStocks(anyString())).thenReturn(List.of(
            new SearchResultDto("AAPL", "Apple Inc.", "USD", "NASDAQ")));

        when(stockService.getScreener(any())).thenReturn(List.of(
            new ScreenerResponseDto("AAPL", "Apple Inc.", 175.30, 0.71, "Technology")));

        when(stockService.getStockDetail(anyString())).thenReturn(
            new StockDetailDto(
                new ProfileDto("AAPL", "Apple Inc.", 175.30, "https://fmp.com/AAPL.png"),
                new QuoteDto(175.30, 1.23, 0.71, 52000000L),
                List.of(new CandlestickDto("2026-03-27 09:30", 174.0, 176.0, 173.5, 175.30, 1500000L)),
                List.of("MSFT", "GOOGL")));

        when(stockService.getFundamentals(anyString())).thenReturn(
            new FundamentalsDto(
                List.of(Map.of("date", "2025-12-31", "revenue", 124300000000L)),
                List.of(Map.of("date", "2025-12-31", "totalAssets", 352000000000L)),
                List.of(Map.of("date", "2025-12-31", "operatingCashFlow", 53800000000L)),
                Map.of("peRatio", 28.5, "roe", 1.47),
                Map.of("altmanZScore", 4.2)));

        when(stockService.getGovernance(anyString())).thenReturn(
            new GovernanceDto(
                List.of(Map.of("name", "Tim Cook", "title", "CEO")),
                List.of(Map.of("date", "2026-02-10", "dividend", 0.25)),
                List.of(Map.of("date", "2026-01-30", "eps", 2.18))));
    }

    @Test
    void search_DeveRetornar200ComSymbolEName_QuandoAutenticado() throws Exception {
        mockMvc.perform(get("/api/v1/stocks/search")
                .param("query", "AAPL")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].symbol").value("AAPL"))
            .andExpect(jsonPath("$[0].name").value("Apple Inc."));
    }

    @Test
    void screener_DeveRetornar200ComChangePercent_QuandoAutenticado() throws Exception {
        mockMvc.perform(get("/api/v1/stocks/screener")
                .param("sector", "Technology")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].symbol").value("AAPL"))
            .andExpect(jsonPath("$[0].changePercent").value(0.71));
    }

    @Test
    void getStockDetail_DeveRetornar200ComCamposContratuais_QuandoAutenticado() throws Exception {
        mockMvc.perform(get("/api/v1/stocks/AAPL")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profile.image").value("https://fmp.com/AAPL.png"))
            .andExpect(jsonPath("$.quote.changePercent").value(0.71))
            .andExpect(jsonPath("$.candlesticks[0].date").value("2026-03-27 09:30"));
    }

    @Test
    void getFundamentals_DeveRetornar200ComSubObjetos_QuandoAutenticado() throws Exception {
        mockMvc.perform(get("/api/v1/stocks/AAPL/fundamentals")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.incomeStatement").isArray())
            .andExpect(jsonPath("$.ratios.peRatio").value(28.5));
    }

    @Test
    void getGovernance_DeveRetornar200ComSubObjetos_QuandoAutenticado() throws Exception {
        mockMvc.perform(get("/api/v1/stocks/AAPL/governance")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.executives[0].name").value("Tim Cook"))
            .andExpect(jsonPath("$.dividends").isArray());
    }

    @Test
    void getStockDetail_DeveRetornar401_QuandoSemToken() throws Exception {
        mockMvc.perform(get("/api/v1/stocks/AAPL"))
            .andExpect(status().isUnauthorized());
    }

    private String obterToken(String username, String password) throws Exception {
        var loginDto = new LoginRequestDto(username, password);
        String body = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginDto)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }
}
