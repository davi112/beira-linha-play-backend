package br.icei.beiralinhaplay.apresentacao.dto;

import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CadastroRequest(
        @NotNull TipoUsuario tipo,
        @NotBlank String nome,
        String apelido,
        String email,
        @NotBlank String senha,
        String cursoOrigem
) {
}
