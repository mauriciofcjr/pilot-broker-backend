package com.pilotbroker.web.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pilotbroker.model.Usuario;
import com.pilotbroker.service.UsuarioService;
import com.pilotbroker.web.dto.usuario.UsuarioResponseDto;
import com.pilotbroker.web.dto.usuario.UsuarioSenhaDto;
import com.pilotbroker.web.mapper.UsuarioMapper;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Usuários", description = "Gerenciamento de usuários")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Buscar usuário por ID")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> buscarPorId(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        // ADMIN acessa qualquer usuário; CLIENTE só acessa o próprio
        Usuario usuario = usuarioService.buscarPorId(id);
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !usuario.getUsername().equals(userDetails.getUsername())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Acesso negado.");
        }
        return ResponseEntity.ok(UsuarioMapper.toDto(usuario));
    }

    @Operation(summary = "Listar todos os usuários (somente ADMIN)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDto>> listarTodos() {
        return ResponseEntity.ok(UsuarioMapper.toListDto(usuarioService.buscarTodos()));
    }

    @Operation(summary = "Alterar senha do usuário")
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> alterarSenha(@PathVariable Long id,
            @Valid @RequestBody UsuarioSenhaDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Usuário só pode alterar a própria senha; ADMIN pode alterar qualquer uma
        Usuario usuario = usuarioService.buscarPorId(id);
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !usuario.getUsername().equals(userDetails.getUsername())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Acesso negado.");
        }
        usuarioService.editarSenha(id, dto.getSenhaAtual(), dto.getNovaSenha(), dto.getConfirmaSenha());
        return ResponseEntity.noContent().build();
    }

}
