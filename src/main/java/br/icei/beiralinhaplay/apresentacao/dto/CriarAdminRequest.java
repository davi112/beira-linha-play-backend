package br.icei.beiralinhaplay.apresentacao.dto;

import jakarta.validation.constraints.NotBlank;

public record CriarAdminRequest(@NotBlank String nome, @NotBlank String senha) {
}
