package br.icei.beiralinhaplay.dominio.modulo;

import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Modulo {

    private UUID id;
    private String nome;
    private UUID cursoId;
    private final List<UUID> atividadeIds = new ArrayList<>();

    public Modulo(UUID id, String nome, UUID cursoId, List<UUID> atividadeIds) {
        definirNome(nome);
        if (cursoId == null) {
            throw new BusinessRuleException("Módulo precisa de um curso");
        }
        this.id = id;
        this.cursoId = cursoId;
        if (atividadeIds != null) {
            this.atividadeIds.addAll(atividadeIds);
        }
    }

    public void definirNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new BusinessRuleException("Informe o nome do módulo");
        }
        this.nome = nome.trim();
    }

    public void definirId(UUID id) {
        this.id = id;
    }

    public UUID id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public UUID cursoId() {
        return cursoId;
    }

    public List<UUID> atividadeIds() {
        return Collections.unmodifiableList(atividadeIds);
    }
}
