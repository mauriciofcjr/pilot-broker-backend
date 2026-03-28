package com.pilotbroker.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pilotbroker.messaging.OrderProducer;
import com.pilotbroker.model.Usuario;
import com.pilotbroker.repository.UsuarioRepository;
import com.pilotbroker.service.OrderService;
import com.pilotbroker.web.dto.login.LoginRequestDto;
import com.pilotbroker.web.dto.trade.OrderListItemDto;
import com.pilotbroker.web.dto.trade.OrderRequestDto;
import com.pilotbroker.web.dto.trade.OrderResponseDto;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TradeControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @MockBean
    private OrderProducer orderProducer;

    @MockBean
    private OrderService orderService;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        usuarioRepository.deleteAll();

        Usuario user = new Usuario();
        user.setUsername("trade-it@email.com");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole(Usuario.Role.ROLE_CLIENTE);
        usuarioRepository.save(user);

        token = obterToken("trade-it@email.com", "123456");

        when(orderService.criarOrdem(anyString(), any(OrderRequestDto.class)))
            .thenReturn(new OrderResponseDto(1L, "PENDING", "Ordem recebida e em processamento"));

        when(orderService.listarOrdens(anyString()))
            .thenReturn(List.of(new OrderListItemDto(
                1L, "AAPL", "BUY", 10, 175.30, "PENDING", "2026-03-27T10:30:00")));
    }

    @Test
    void postOrder_DeveRetornar202ComCamposContratuais_QuandoAutenticado() throws Exception {
        OrderRequestDto request = new OrderRequestDto("AAPL", "BUY", 10, 175.30);

        mockMvc.perform(post("/api/v1/trade/order")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.orderId").value(1))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.message").value("Ordem recebida e em processamento"));
    }

    @Test
    void getOrders_DeveRetornar200ComListaDoUsuario_QuandoAutenticado() throws Exception {
        mockMvc.perform(get("/api/v1/trade/orders")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].symbol").value("AAPL"))
            .andExpect(jsonPath("$[0].status").value("PENDING"))
            .andExpect(jsonPath("$[0].dataCriacao").value("2026-03-27T10:30:00"));
    }

    @Test
    void postOrder_DeveRetornar401_QuandoSemToken() throws Exception {
        OrderRequestDto request = new OrderRequestDto("AAPL", "BUY", 10, 175.30);

        mockMvc.perform(post("/api/v1/trade/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void postOrder_DeveRetornar422_QuandoBodyInvalido() throws Exception {
        // quantidade = 0 viola @Min(1)
        OrderRequestDto invalid = new OrderRequestDto("AAPL", "BUY", 0, 175.30);

        mockMvc.perform(post("/api/v1/trade/order")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void getOrders_DeveRetornar401_QuandoSemToken() throws Exception {
        mockMvc.perform(get("/api/v1/trade/orders"))
            .andExpect(status().isUnauthorized());
    }

    private String obterToken(String username, String password) throws Exception {
        var loginDto = new LoginRequestDto(username, password);
        String body = mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginDto)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }
}
