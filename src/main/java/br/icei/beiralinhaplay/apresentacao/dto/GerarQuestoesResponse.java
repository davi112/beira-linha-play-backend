package br.icei.beiralinhaplay.apresentacao.dto;

import java.util.List;

public record GerarQuestoesResponse(List<QuestaoGeradaResponse> questoes) {

    public record QuestaoGeradaResponse(
            String enunciado,
            int valor,
            List<AlternativaGeradaResponse> alternativas
    ) {
    }

    public record AlternativaGeradaResponse(String descricao, boolean correta) {
    }
}
