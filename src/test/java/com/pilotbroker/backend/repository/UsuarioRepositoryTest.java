package com.pilotbroker.backend.repository;

import com.pilotbroker.backend.config.SpringJpaAuditingConfig;
import com.pilotbroker.backend.model.Usuario;
import com.pilotbroker.backend.model.Usuario.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
@Import(SpringJpaAuditingConfig.class)
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void deveSalvarUsuarioComSucesso() {
        Usuario usuario = new Usuario("admin@pilotbroker.com", "senha123", Role.ROLE_ADMIN);

        Usuario salvo = usuarioRepository.save(usuario);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getUsername()).isEqualTo("admin@pilotbroker.com");
        assertThat(salvo.getRole()).isEqualTo(Role.ROLE_ADMIN);
        assertThat(salvo.getDataCriacao()).isNotNull();
    }

    @Test
    void deveLancarExcecaoAoSalvarUsernameRepetido() {
        usuarioRepository.save(new Usuario("duplicado@pilotbroker.com", "senha123", Role.ROLE_CLIENTE));

        assertThrows(DataIntegrityViolationException.class, () ->
                usuarioRepository.saveAndFlush(
                        new Usuario("duplicado@pilotbroker.com", "outrasenha", Role.ROLE_CLIENTE)
                )
        );
    }

    @Test
    void deveBuscarUsuarioPorUsername() {
        usuarioRepository.save(new Usuario("user@pilotbroker.com", "senha123", Role.ROLE_CLIENTE));

        Optional<Usuario> encontrado = usuarioRepository.findByUsername("user@pilotbroker.com");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getUsername()).isEqualTo("user@pilotbroker.com");
    }

    @Test
    void deveBuscarUsuarioPorId() {
        Usuario salvo = usuarioRepository.save(new Usuario("user2@pilotbroker.com", "senha123", Role.ROLE_CLIENTE));

        Optional<Usuario> encontrado = usuarioRepository.findById(salvo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getId()).isEqualTo(salvo.getId());
    }
}
