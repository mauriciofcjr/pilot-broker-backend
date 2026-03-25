package com.pilotbroker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import com.pilotbroker.shared.security.JwtTokenProvider;
import com.pilotbroker.web.dto.login.LoginRequestDto;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void autenticar_DeveRetornarToken_QuandoCredenciaisValidas() {
        var loginDto = new LoginRequestDto("user@test.com", "123456");
        Authentication mockAuth = mock(Authentication.class);

        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(jwtTokenProvider.gerarToken("user@test.com")).thenReturn("jwt.token.aqui");

        String token = authService.autenticar(loginDto);

        assertThat(token).isEqualTo("jwt.token.aqui");
    }

    @Test
    void autenticar_DeveLancarExcecao_QuandoCredenciaisInvalidas() {
        var loginDto = new LoginRequestDto("user@test.com", "errado");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        assertThatThrownBy(() -> authService.autenticar(loginDto))
                .isInstanceOf(BadCredentialsException.class);
    }

}
