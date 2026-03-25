package com.pilotbroker.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pilotbroker.model.Usuario;
import com.pilotbroker.service.AuthService;
import com.pilotbroker.service.UsuarioService;
import com.pilotbroker.shared.exception.ErrorMessage;
import com.pilotbroker.web.dto.login.LoginRequestDto;
import com.pilotbroker.web.dto.login.LoginResponseDto;
import com.pilotbroker.web.dto.usuario.UsuarioCreateDto;
import com.pilotbroker.web.dto.usuario.UsuarioResponseDto;
import com.pilotbroker.web.mapper.UsuarioMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Autenticação", description = "Recursos de autenticação e cadastro de usuário")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    @Operation(summary = "Realizar login",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Token JWT gerado"),
                   @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                                content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
               })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        String token = authService.autenticar(dto);
        return ResponseEntity.ok(new LoginResponseDto(token));
    }

    @Operation(summary = "Cadastrar novo usuário")
    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDto> register(@Valid @RequestBody UsuarioCreateDto dto) {
        Usuario salvo = usuarioService.salvar(usuarioMapper.toUsuario(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioMapper.toDto(salvo));
    }

}
