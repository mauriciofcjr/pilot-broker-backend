package com.pilotbroker.web.mapper;

import com.pilotbroker.model.Usuario;
import com.pilotbroker.web.dto.usuario.UsuarioCreateDto;
import com.pilotbroker.web.dto.usuario.UsuarioResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UsuarioMapper {

    private final ModelMapper modelMapper;

    public Usuario toUsuario(UsuarioCreateDto dto) {
        return modelMapper.map(dto, Usuario.class);
    }

    public UsuarioResponseDto toDto(Usuario usuario) {
        return new UsuarioResponseDto(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getRole().name().substring("ROLE_".length())
        );
    }

    public List<UsuarioResponseDto> toListDto(List<Usuario> usuarios) {
        return usuarios.stream().map(this::toDto).collect(Collectors.toList());
    }
}
