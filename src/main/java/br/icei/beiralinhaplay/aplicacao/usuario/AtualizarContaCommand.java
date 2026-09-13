package br.icei.beiralinhaplay.aplicacao.usuario;

public record AtualizarContaCommand(
        String nome,
        String apelido,
        String email,
        String senha
) {
}
