package br.icei.beiralinhaplay.apresentacao.dto;

public record ResultadoTentativaResponse(
        TentativaResponse tentativa,
        int tentativasUsadas,
        int melhorPontuacao,
        int pontosDelta,
        int pontosTotais,
        boolean concluida
) {
}
