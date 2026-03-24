package com.pilotbroker.service;

import com.pilotbroker.exception.PasswordInvalidException;
import com.pilotbroker.exception.UsernameUniqueViolationException;
import com.pilotbroker.model.Usuario;
import com.pilotbroker.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario salvar(Usuario usuario) {
        usuarioRepository.findByUsername(usuario.getUsername())
                .ifPresent(u -> {
                    throw new UsernameUniqueViolationException(usuario.getUsername());
                });
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Usuário id=%d não encontrado.", id)));
    }

    @Transactional(readOnly = true)
    public List<Usuario> buscarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Usuário '%s' não encontrado.", username)));
    }

    @Transactional
    public void editarSenha(Long id, String senhaAtual, String novaSenha, String confirmaSenha) {
        if (!novaSenha.equals(confirmaSenha)) {
            throw new PasswordInvalidException("As novas senhas não coincidem.");
        }
        Usuario usuario = buscarPorId(id);
        if (!passwordEncoder.matches(senhaAtual, usuario.getPassword())) {
            throw new PasswordInvalidException("Senha atual incorreta.");
        }
        usuario.setPassword(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }
}
