package br.icei.beiralinhaplay.dominio.compartilhado;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String mensagem) {
        super(mensagem);
    }
}
