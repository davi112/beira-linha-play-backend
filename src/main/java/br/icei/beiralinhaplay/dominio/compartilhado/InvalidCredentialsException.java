package br.icei.beiralinhaplay.dominio.compartilhado;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Credenciais inválidas");
    }

    public InvalidCredentialsException(String mensagem) {
        super(mensagem);
    }
}
