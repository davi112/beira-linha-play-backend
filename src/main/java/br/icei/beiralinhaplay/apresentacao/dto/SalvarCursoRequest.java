package br.icei.beiralinhaplay.apresentacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SalvarCursoRequest(
        @NotBlank String nome,
        @NotEmpty List<String> monitorIds
) {
}
