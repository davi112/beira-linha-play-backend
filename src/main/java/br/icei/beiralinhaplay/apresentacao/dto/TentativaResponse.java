package br.icei.beiralinhaplay.apresentacao.dto;

import java.util.List;

public record TentativaResponse(
        String id,
        String dataEnvio,
        int pontuacaoObtida,
        String alunoId,
        String atividadeId,
        List<RespostaItem> respostas
) {
    public record RespostaItem(String id, boolean correta, String questaoId, String alternativaId) {
    }
}
