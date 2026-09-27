package br.icei.beiralinhaplay.apresentacao.dto;

public record EventoDisponivelResponse(
        String referencia,
        String nome,
        String inicio,
        String fim
) {
}
