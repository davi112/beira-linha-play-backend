package br.icei.beiralinhaplay.apresentacao.dto;

import jakarta.validation.constraints.NotBlank;

public record SalvarModuloRequest(@NotBlank String nome) {
}
