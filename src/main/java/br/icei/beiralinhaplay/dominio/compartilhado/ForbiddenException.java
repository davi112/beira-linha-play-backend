package br.icei.beiralinhaplay.dominio.compartilhado;

public class ForbiddenException extends DomainException {

    public ForbiddenException() {
        super("Acesso negado");
    }

    public ForbiddenException(String mensagem) {
        super(mensagem);
    }
}
