package com.pilotbroker.service;

import com.pilotbroker.shared.exception.UsernameUniqueViolationException;
import com.pilotbroker.shared.exception.PasswordInvalidException;
import com.pilotbroker.model.Usuario;
import com.pilotbroker.repository.UsuarioRepository;
import com.pilotbroker.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioValido;

    @BeforeEach
    void setUp() {
        usuarioValido = new Usuario();
        usuarioValido.setId(1L);
        usuarioValido.setUsername("teste@email.com");
        usuarioValido.setPassword("hashed");
        usuarioValido.setRole(Usuario.Role.ROLE_CLIENTE);
    }

    @Test
    void salvar_DeveRetornarUsuarioCriado_QuandoDadosValidos() {
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioValido);

        Usuario resultado = usuarioService.salvar(usuarioValido);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getUsername()).isEqualTo("teste@email.com");
        verify(passwordEncoder).encode(any());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void salvar_DeveLancarExcecao_QuandoUsernameJaExiste() {
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.of(usuarioValido));

        assertThatThrownBy(() -> usuarioService.salvar(usuarioValido))
                .isInstanceOf(UsernameUniqueViolationException.class)
                .hasMessageContaining("teste@email.com");
    }

    @Test
    void buscarPorId_DeveRetornarUsuario_QuandoIdExiste() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));

        Usuario resultado = usuarioService.buscarPorId(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    void buscarPorId_DeveLancarExcecao_QuandoIdNaoExiste() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void editarSenha_DeveAtualizarSenha_QuandoSenhaAtualCorreta() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioValido));
        when(passwordEncoder.matches("123456", "hashed")).thenReturn(true);
        when(passwordEncoder.encode("novaSenha")).thenReturn("novaHashed");

        usuarioService.editarSenha(1L, "123456", "novaSenha", "novaSenha");

        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void editarSenha_DeveLancarExcecao_QuandoSenhasNovasNaoCoincidem() {
        assertThatThrownBy(() -> usuarioService.editarSenha(1L, "123456", "nova1", "nova2"))
                .isInstanceOf(PasswordInvalidException.class);
    }
}
