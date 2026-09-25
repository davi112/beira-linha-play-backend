package br.icei.beiralinhaplay.aplicacao.medalha;

public record SalvarMedalhaCommand(
        String nome,
        int pontosMin,
        byte[] imagem,
        String contentType,
        String nomeArquivo
) {
}
