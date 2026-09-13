package br.icei.beiralinhaplay.dominio.alternativa;

import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;

import java.util.UUID;

public class Alternativa {

    private UUID id;
    private String descricao;
    private boolean correta;

    public Alternativa(UUID id, String descricao, boolean correta) {
        definirDescricao(descricao);
        this.id = id;
        this.correta = correta;
    }

    public void definirDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new BusinessRuleException("Informe a descrição da alternativa");
        }
        this.descricao = descricao.trim();
    }

    public void definirId(UUID id) {
        this.id = id;
    }

    public UUID id() {
        return id;
    }

    public String descricao() {
        return descricao;
    }

    public boolean correta() {
        return correta;
    }
}
