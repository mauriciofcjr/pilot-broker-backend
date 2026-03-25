package com.pilotbroker.web.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.pilotbroker.service.MarketService;
import com.pilotbroker.web.dto.market.ActivelyTradingDto;
import com.pilotbroker.web.dto.market.DashboardResponseDto;
import com.pilotbroker.web.dto.market.EconomicEventDto;
import com.pilotbroker.web.dto.market.IndexQuoteDto;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pilotbroker.model.Usuario;
import com.pilotbroker.repository.UsuarioRepository;
import com.pilotbroker.web.dto.login.LoginRequestDto;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MarketControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private MarketService marketService;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        usuarioRepository.deleteAll();

        Usuario user = new Usuario();
        user.setUsername("user-it@email.com");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole(Usuario.Role.ROLE_CLIENTE);
        usuarioRepository.save(user);

        token = obterToken("user-it@email.com", "123456");

        when(marketService.getDashboard()).thenReturn(new DashboardResponseDto(
                List.of(new IndexQuoteDto("^GSPC", "S&P 500", 5000.0, 1.5)),
                List.of(new ActivelyTradingDto("AAPL", "Apple Inc.", 189.0, 2.0, 80000000L)),
                List.of(new EconomicEventDto("CPI Data", "2026-03-25", "High"))
        ));
    }

    @Test
    void getDashboard_DeveRetornar200_QuandoAutenticado() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indices").isArray())
                .andExpect(jsonPath("$.indices[0].symbol").value("^GSPC"))
                .andExpect(jsonPath("$.activelyTrading").isArray())
                .andExpect(jsonPath("$.activelyTrading[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$.economicCalendar").isArray())
                .andExpect(jsonPath("$.economicCalendar[0].event").value("CPI Data"));
    }

    @Test
    void getDashboard_DeveRetornar401_QuandoSemToken() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    private String obterToken(String username, String password) throws Exception {
        var loginDto = new LoginRequestDto(username, password);
        String responseBody = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("token").asText();
    }
}
