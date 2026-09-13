package br.icei.beiralinhaplay.apresentacao.dto;

public record RankingResponse(
        int posicao,
        String id,
        String nome,
        String apelido,
        int pontos,
        String imagemPerfil
) {
}
