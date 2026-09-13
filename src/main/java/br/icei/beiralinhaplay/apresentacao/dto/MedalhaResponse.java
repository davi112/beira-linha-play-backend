package br.icei.beiralinhaplay.apresentacao.dto;

public record MedalhaResponse(
        String id,
        String nome,
        String imagemUrl,
        int pontosMin,
        boolean conquistada
) {
}
