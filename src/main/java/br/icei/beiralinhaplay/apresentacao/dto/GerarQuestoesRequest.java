package br.icei.beiralinhaplay.apresentacao.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record GerarQuestoesRequest(
        @NotBlank String mensagem,
        @Min(1) @Max(10) Integer quantidadeQuestoes
) {
}
