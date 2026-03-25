package com.pilotbroker.web.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UsuarioSenhaDto {

    @NotBlank
    private String senhaAtual;

    @NotBlank
    @Size(min = 6, max = 6, message = "Nova senha deve ter exatamente 6 caracteres")
    private String novaSenha;

    @NotBlank
    @Size(min = 6, max = 6, message = "Confirmação de senha deve ter exatamente 6 caracteres")
    private String confirmaSenha;
}
