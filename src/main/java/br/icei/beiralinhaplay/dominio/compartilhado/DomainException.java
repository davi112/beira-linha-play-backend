package br.icei.beiralinhaplay.dominio.compartilhado;

public class DomainException extends RuntimeException {

    public DomainException(String mensagem) {
        super(mensagem);
    }
}
