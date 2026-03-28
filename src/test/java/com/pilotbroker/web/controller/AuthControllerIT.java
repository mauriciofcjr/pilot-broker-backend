package com.pilotbroker.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pilotbroker.messaging.OrderProducer;
import com.pilotbroker.web.dto.login.LoginRequestDto;
import com.pilotbroker.web.dto.usuario.UsuarioCreateDto;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderProducer orderProducer;

    @Test
    void register_DeveRetornar201_QuandoDadosValidos() throws Exception {
        var dto = new UsuarioCreateDto("novo@email.com", "123456");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("novo@email.com"))
                .andExpect(jsonPath("$.role").value("CLIENTE"));
    }

    @Test
    void register_DeveRetornar422_QuandoEmailInvalido() throws Exception {
        var dto = new UsuarioCreateDto("nao-e-email", "123456");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void register_DeveRetornar422_QuandoSenhaMenorQue6Chars() throws Exception {
        var dto = new UsuarioCreateDto("user@email.com", "123");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void login_DeveRetornar200ComToken_QuandoCredenciaisValidas() throws Exception {
        // Primeiro cadastrar
        var createDto = new UsuarioCreateDto("login@email.com", "123456");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated());

        // Então fazer login
        var loginDto = new LoginRequestDto("login@email.com", "123456");
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_DeveRetornar401_QuandoCredenciaisInvalidas() throws Exception {
        var loginDto = new LoginRequestDto("naoexiste@email.com", "123456");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_DeveRetornar409_QuandoUsernameJaExiste() throws Exception {
        var dto = new UsuarioCreateDto("duplicado@email.com", "123456");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

}
