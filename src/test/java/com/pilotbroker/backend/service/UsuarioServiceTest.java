package com.pilotbroker.backend.service;

import com.pilotbroker.backend.exception.PasswordInvalidException;
import com.pilotbroker.backend.exception.UsernameUniqueViolationException;
import com.pilotbroker.backend.model.Usuario;
import com.pilotbroker.backend.model.Usuario.Role;
import com.pilotbroker.backend.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deveCriarUsuarioComSucesso() {
        Usuario usuario = new Usuario("novo@pilotbroker.com", "senha123", Role.ROLE_CLIENTE);
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            return u;
        });

        Usuario salvo = usuarioService.salvar(usuario);

        assertThat(salvo.getPassword()).isEqualTo("$2a$hash");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveLancarExcecaoAoSalvarEmailDuplicado() {
        Usuario usuario = new Usuario("duplicado@pilotbroker.com", "senha123", Role.ROLE_CLIENTE);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$hash");
        when(usuarioRepository.save(any(Usuario.class))).thenThrow(DataIntegrityViolationException.class);

        assertThrows(UsernameUniqueViolationException.class, () -> usuarioService.salvar(usuario));
    }

    @Test
    void deveBuscarUsuarioPorId() {
        Usuario usuario = new Usuario("user@pilotbroker.com", "$2a$hash", Role.ROLE_CLIENTE);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Usuario encontrado = usuarioService.buscarPorId(1L);

        assertThat(encontrado.getUsername()).isEqualTo("user@pilotbroker.com");
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> usuarioService.buscarPorId(99L));
    }

    @Test
    void deveAlterarSenhaComSucesso() {
        Usuario usuario = new Usuario("user@pilotbroker.com", "$2a$hashAtual", Role.ROLE_CLIENTE);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaAtual", "$2a$hashAtual")).thenReturn(true);
        when(passwordEncoder.encode("novaSenha")).thenReturn("$2a$hashNovo");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario atualizado = usuarioService.editarSenha(1L, "senhaAtual", "novaSenha", "novaSenha");

        assertThat(atualizado.getPassword()).isEqualTo("$2a$hashNovo");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveLancarExcecaoSenhaAtualIncorreta() {
        Usuario usuario = new Usuario("user@pilotbroker.com", "$2a$hashAtual", Role.ROLE_CLIENTE);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaErrada", "$2a$hashAtual")).thenReturn(false);

        assertThrows(PasswordInvalidException.class,
                () -> usuarioService.editarSenha(1L, "senhaErrada", "novaSenha", "novaSenha"));
    }

    @Test
    void deveLancarExcecaoSenhasNovasNaoConferem() {
        assertThrows(PasswordInvalidException.class,
                () -> usuarioService.editarSenha(1L, "senhaAtual", "novaSenha", "senhasDiferente"));
    }

    @Test
    void deveListarTodosOsUsuarios() {
        List<Usuario> lista = List.of(
                new Usuario("admin@pilotbroker.com", "$2a$hash", Role.ROLE_ADMIN),
                new Usuario("user@pilotbroker.com", "$2a$hash", Role.ROLE_CLIENTE)
        );
        when(usuarioRepository.findAll()).thenReturn(lista);

        List<Usuario> resultado = usuarioService.buscarTodos();

        assertThat(resultado).hasSize(2);
    }
}
