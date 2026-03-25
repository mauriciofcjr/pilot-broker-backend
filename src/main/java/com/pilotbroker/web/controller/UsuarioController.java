package com.pilotbroker.web.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pilotbroker.model.Usuario;
import com.pilotbroker.service.UsuarioService;
import com.pilotbroker.web.dto.usuario.UsuarioResponseDto;
import com.pilotbroker.web.dto.usuario.UsuarioRoleDto;
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
    private final UsuarioMapper usuarioMapper;

    @Operation(summary = "Buscar usuário por ID")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> buscarPorId(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioService.buscarPorId(id);
        verificarAcesso(usuario, userDetails);
        return ResponseEntity.ok(usuarioMapper.toDto(usuario));
    }

    @Operation(summary = "Listar todos os usuários (somente ADMIN)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDto>> listarTodos() {
        return ResponseEntity.ok(usuarioMapper.toListDto(usuarioService.buscarTodos()));
    }

    @Operation(summary = "Alterar senha do usuário")
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> alterarSenha(@PathVariable Long id,
            @Valid @RequestBody UsuarioSenhaDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioService.buscarPorId(id);
        verificarAcesso(usuario, userDetails);
        usuarioService.editarSenha(id, dto.getSenhaAtual(), dto.getNovaSenha(), dto.getConfirmaSenha());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Excluir usuário por ID (somente ADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Alterar role do usuário (somente ADMIN)")
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDto> alterarRole(@PathVariable Long id,
            @Valid @RequestBody UsuarioRoleDto dto) {
        Usuario atualizado = usuarioService.editarRole(id, dto.getRole());
        return ResponseEntity.ok(usuarioMapper.toDto(atualizado));
    }

    private void verificarAcesso(Usuario usuario, UserDetails userDetails) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !usuario.getUsername().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("Acesso negado.");
        }
    }
}
