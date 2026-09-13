package br.icei.beiralinhaplay.apresentacao.dto;

public record AtualizarContaRequest(
        String nome,
        String apelido,
        String email,
        String senha
) {
}
