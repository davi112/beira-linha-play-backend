package br.icei.beiralinhaplay.apresentacao.dto;

import jakarta.validation.constraints.NotBlank;

public record SalvarMedalhaRequest(
        @NotBlank String nome,
        @NotBlank String imagemUrl,
        int pontosMin
) {
}
