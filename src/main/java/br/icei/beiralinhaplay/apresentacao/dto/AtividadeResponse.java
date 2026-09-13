package br.icei.beiralinhaplay.apresentacao.dto;

import java.util.List;

public record AtividadeResponse(
        String id,
        String titulo,
        int quantQuestoes,
        String moduloId,
        List<QuestaoResposta> questoes
) {
    public record QuestaoResposta(String id, String enunciado, int valor, List<AlternativaResposta> alternativas) {
    }

    public record AlternativaResposta(String id, String descricao, Boolean correta) {
    }
}
