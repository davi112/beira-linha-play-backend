package br.icei.beiralinhaplay.apresentacao.dto;

import jakarta.validation.constraints.NotBlank;

public record ImportarInscritosRequest(@NotBlank String referencia) {
}
