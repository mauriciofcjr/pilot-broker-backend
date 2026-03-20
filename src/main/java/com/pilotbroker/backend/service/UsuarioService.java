package com.pilotbroker.backend.service;

import com.pilotbroker.backend.exception.PasswordInvalidException;
import com.pilotbroker.backend.exception.UsernameUniqueViolationException;
import com.pilotbroker.backend.model.Usuario;
import com.pilotbroker.backend.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public Usuario salvar(Usuario usuario) {
        try {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            return usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException ex) {
            throw new UsernameUniqueViolationException(
                    String.format("Username '%s' já está cadastrado.", usuario.getUsername()));
        }
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Usuário id=%d não encontrado.", id)));
    }

    @Transactional
    public Usuario editarSenha(Long id, String senhaAtual, String novaSenha, String confirmaSenha) {
        validarConfirmacaoSenha(novaSenha, confirmaSenha);
        Usuario usuario = buscarPorId(id);
        validarSenhaAtual(senhaAtual, usuario.getPassword());
        usuario.setPassword(passwordEncoder.encode(novaSenha));
        return usuarioRepository.save(usuario);
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

    private void validarConfirmacaoSenha(String novaSenha, String confirmaSenha) {
        if (!novaSenha.equals(confirmaSenha)) {
            throw new PasswordInvalidException("Nova senha e confirmação de senha não conferem.");
        }
    }

    private void validarSenhaAtual(String senhaAtual, String hashAtual) {
        if (!passwordEncoder.matches(senhaAtual, hashAtual)) {
            throw new PasswordInvalidException("Senha atual incorreta.");
        }
    }
}
