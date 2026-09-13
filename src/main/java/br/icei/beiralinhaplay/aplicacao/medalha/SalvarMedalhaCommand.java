package br.icei.beiralinhaplay.aplicacao.medalha;

public record SalvarMedalhaCommand(String nome, String imagemUrl, int pontosMin) {
}
