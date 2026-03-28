package com.pilotbroker.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import com.pilotbroker.messaging.OrderProducer;
import com.pilotbroker.model.Usuario;
import com.pilotbroker.repository.UsuarioRepository;
import com.pilotbroker.service.UsuarioService;
import com.pilotbroker.web.dto.login.LoginRequestDto;
import com.pilotbroker.web.dto.usuario.UsuarioCreateDto;
import com.pilotbroker.web.dto.usuario.UsuarioRoleDto;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UsuarioControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private OrderProducer orderProducer;

    @Autowired
    private UsuarioService usuarioService;

    private String adminToken;
    private String clienteToken;
    private Long clienteId;

    @BeforeEach
    void setUp() throws Exception {
        usuarioRepository.deleteAll();

        // Criar admin diretamente (register sempre cria ROLE_CLIENTE)
        Usuario admin = new Usuario();
        admin.setUsername("admin-it@email.com");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setRole(Usuario.Role.ROLE_ADMIN);
        usuarioRepository.save(admin);
        adminToken = obterToken("admin-it@email.com", "123456");

        // Criar cliente via /register
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new UsuarioCreateDto("cliente-it@email.com", "123456"))))
                .andExpect(status().isCreated());

        clienteToken = obterToken("cliente-it@email.com", "123456");
        clienteId = usuarioService.buscarPorUsername("cliente-it@email.com").getId();
    }

    // --- DELETE /{id} ---

    @Test
    void delete_DeveRetornar204_QuandoAdminExcluiUsuario() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + clienteId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_DeveRetornar404_QuandoIdNaoExiste() throws Exception {
        mockMvc.perform(delete("/api/v1/users/999999")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_DeveRetornar403_QuandoClienteTenta() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + clienteId)
                .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_DeveRetornar401_QuandoSemToken() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + clienteId))
                .andExpect(status().isUnauthorized());
    }

    // --- PATCH /{id}/role ---

    @Test
    void patchRole_DeveRetornar200ComRoleAtualizada_QuandoAdminAltera() throws Exception {
        var dto = new UsuarioRoleDto(Usuario.Role.ROLE_ADMIN);

        mockMvc.perform(patch("/api/v1/users/" + clienteId + "/role")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteId))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void patchRole_DeveRetornar404_QuandoIdNaoExiste() throws Exception {
        var dto = new UsuarioRoleDto(Usuario.Role.ROLE_ADMIN);

        mockMvc.perform(patch("/api/v1/users/999999/role")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchRole_DeveRetornar403_QuandoClienteTenta() throws Exception {
        var dto = new UsuarioRoleDto(Usuario.Role.ROLE_ADMIN);

        mockMvc.perform(patch("/api/v1/users/" + clienteId + "/role")
                .header("Authorization", "Bearer " + clienteToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void patchRole_DeveRetornar422_QuandoRoleNula() throws Exception {
        mockMvc.perform(patch("/api/v1/users/" + clienteId + "/role")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\": null}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void patchRole_DeveRetornar400_QuandoRoleInvalida() throws Exception {
        mockMvc.perform(patch("/api/v1/users/" + clienteId + "/role")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\": \"ROLE_INVALIDA\"}"))
                .andExpect(status().isBadRequest());
    }

    private String obterToken(String username, String password) throws Exception {
        var loginDto = new LoginRequestDto(username, password);
        String responseBody = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("token").asText();
    }
}
