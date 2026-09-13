package br.icei.beiralinhaplay.apresentacao.dto;

import jakarta.validation.constraints.NotBlank;

public record InscreverCursoRequest(@NotBlank String codigoAcesso) {
}
