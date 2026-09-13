package br.icei.beiralinhaplay.apresentacao.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SalvarAtividadeRequest(
        @NotBlank String titulo,
        @NotEmpty @Valid List<QuestaoEntrada> questoes
) {
    public record QuestaoEntrada(
            @NotBlank String enunciado,
            int valor,
            @NotEmpty @Valid List<AlternativaEntrada> alternativas
    ) {
    }

    public record AlternativaEntrada(@NotBlank String descricao, boolean correta) {
    }
}
