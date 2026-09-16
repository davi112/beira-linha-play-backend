package br.icei.beiralinhaplay.dominio.compartilhado;

public class BadGatewayException extends DomainException {

    public BadGatewayException(String mensagem) {
        super(mensagem);
    }
}
