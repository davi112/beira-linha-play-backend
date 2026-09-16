package br.icei.beiralinhaplay.dominio.compartilhado;

public class ServiceUnavailableException extends DomainException {

    public ServiceUnavailableException(String mensagem) {
        super(mensagem);
    }
}
