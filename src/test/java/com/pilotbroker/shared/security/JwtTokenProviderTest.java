package com.pilotbroker.shared.security;

import com.pilotbroker.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
            "test-secret-key-for-unit-testing-only-min-256-bits-abcdef",
            86400000L
        );
    }

    @Test
    void gerarToken_DeveRetornarTokenValido_QuandoUsernameForecido() {
        String token = jwtTokenProvider.gerarToken("user@test.com");

        assertThat(token).isNotBlank();
    }

    @Test
    void extrairUsername_DeveRetornarUsername_QuandoTokenValido() {
        String token = jwtTokenProvider.gerarToken("user@test.com");

        String username = jwtTokenProvider.extrairUsername(token);

        assertThat(username).isEqualTo("user@test.com");
    }

    @Test
    void validarToken_DeveRetornarTrue_QuandoTokenValido() {
        String token = jwtTokenProvider.gerarToken("user@test.com");

        boolean valido = jwtTokenProvider.validarToken(token);

        assertThat(valido).isTrue();
    }

    @Test
    void validarToken_DeveRetornarFalse_QuandoTokenInvalido() {
        boolean valido = jwtTokenProvider.validarToken("token.invalido.aqui");

        assertThat(valido).isFalse();
    }
}
