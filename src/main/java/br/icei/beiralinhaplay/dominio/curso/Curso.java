package br.icei.beiralinhaplay.dominio.curso;

import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Curso {

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private UUID id;
    private String nome;
    private String codigoAcesso;
    private final List<UUID> monitorIds = new ArrayList<>();
    private final List<UUID> moduloIds = new ArrayList<>();

    public Curso(UUID id, String nome, String codigoAcesso, List<UUID> monitorIds, List<UUID> moduloIds) {
        definirNome(nome);
        this.id = id;
        this.codigoAcesso = codigoAcesso == null || codigoAcesso.isBlank()
                ? gerarCodigoAcesso(nome)
                : codigoAcesso.trim().toUpperCase(Locale.ROOT);
        if (monitorIds != null) {
            this.monitorIds.addAll(monitorIds);
        }
        if (moduloIds != null) {
            this.moduloIds.addAll(moduloIds);
        }
    }

    public void definirNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new BusinessRuleException("Informe o nome do curso");
        }
        this.nome = nome.trim();
    }

    public void definirMonitores(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessRuleException("Informe ao menos um monitor");
        }
        monitorIds.clear();
        monitorIds.addAll(ids.stream().distinct().toList());
    }

    public boolean codigoConfere(String codigo) {
        return codigo != null && codigoAcesso.equalsIgnoreCase(codigo.trim());
    }

    public void regenerarCodigoAcesso() {
        this.codigoAcesso = gerarCodigoAcesso(nome);
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

    public String codigoAcesso() {
        return codigoAcesso;
    }

    public List<UUID> monitorIds() {
        return Collections.unmodifiableList(monitorIds);
    }

    public List<UUID> moduloIds() {
        return Collections.unmodifiableList(moduloIds);
    }

    public static String gerarCodigoAcesso(String nome) {
        String prefixo = nome == null || nome.isBlank()
                ? "CURSO"
                : nome.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        if (prefixo.length() > 6) {
            prefixo = prefixo.substring(0, 6);
        }
        if (prefixo.isBlank()) {
            prefixo = "CURSO";
        }
        StringBuilder sufixo = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sufixo.append(ALFABETO.charAt(ALEATORIO.nextInt(ALFABETO.length())));
        }
        return prefixo + "-" + sufixo;
    }
}
