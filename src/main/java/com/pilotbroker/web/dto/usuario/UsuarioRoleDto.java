package com.pilotbroker.web.dto.usuario;

import com.pilotbroker.model.Usuario;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRoleDto {

    @NotNull(message = "O campo role não pode ser nulo")
    private Usuario.Role role;
}
