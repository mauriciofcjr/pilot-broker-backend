package com.pilotbroker.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.pilotbroker.shared.security.JwtTokenProvider;
import com.pilotbroker.web.dto.login.LoginRequestDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public String autenticar(LoginRequestDto dto){
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
        return jwtTokenProvider.gerarToken(dto.getUsername());
    }

}
