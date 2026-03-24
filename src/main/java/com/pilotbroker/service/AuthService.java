package com.pilotbroker.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.pilotbroker.repository.UsuarioRepository;
import com.pilotbroker.shared.security.JwtTokenProvider;
import com.pilotbroker.web.dto.login.LoginRequestDto;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioRepository usuarioRepository;

    public String autenticar(LoginRequestDto dto){
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));

        var usuario = usuarioRepository.findByUsername(dto.getUsername())
        .orElseThrow(() -> new EntityNotFoundException(dto.getUsername()));    

        return jwtTokenProvider.gerarToken(usuario.getUsername());
    }

}
