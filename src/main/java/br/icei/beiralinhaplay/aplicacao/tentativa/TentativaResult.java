package br.icei.beiralinhaplay.aplicacao.tentativa;

import br.icei.beiralinhaplay.dominio.atividade.Atividade;
import br.icei.beiralinhaplay.dominio.tentativa.Tentativa;

public record TentativaResult(
        Tentativa tentativa,
        int tentativasUsadas,
        int melhorPontuacao,
        int pontosDelta,
        int pontosTotais,
        boolean concluida
) {
    public static TentativaResult de(Atividade atividade, Tentativa tentativa, int usadas, int melhorAnterior, int pontosTotais) {
        int melhor = Math.max(melhorAnterior, tentativa.pontuacaoObtida());
        return new TentativaResult(
                tentativa,
                usadas,
                melhor,
                tentativa.pontosDelta(melhorAnterior),
                pontosTotais,
                atividade.concluida(usadas, melhor)
        );
    }
}
