package br.icei.beiralinhaplay.dominio.autenticacao;

public interface PasswordHasher {

    String codificar(String senhaCrua);

    boolean confere(String senhaCrua, String senhaHash);
}
