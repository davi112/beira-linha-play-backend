package br.icei.beiralinhaplay.apresentacao.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record EnviarTentativaRequest(@NotEmpty @Valid List<RespostaEntrada> respostas) {
    public record RespostaEntrada(String questaoId, String alternativaId) {
    }
}
