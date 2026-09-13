package br.icei.beiralinhaplay.apresentacao.dto;

import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull TipoUsuario tipo,
        String apelido,
        String email,
        String nome,
        @NotBlank String senha
) {
}
