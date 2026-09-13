package br.icei.beiralinhaplay.dominio.autenticacao;

public interface RefreshTokenGenerator {

    String gerarTokenOpaco();

    String hash(String tokenOpaco);
}
