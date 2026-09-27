package br.icei.beiralinhaplay.dominio.usuario;

import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class Usuario {

    private UUID id;
    private String nome;
    private String email;
    private String senhaHash;
    private boolean deveDefinirSenha;
    private LocalDate acessoExpiraEm;
    private final List<UUID> cursoIds = new ArrayList<>();

    protected Usuario(UUID id, String nome, String email, String senhaHash, List<UUID> cursoIds) {
        definirNome(nome);
        this.id = id;
        this.email = normalizarEmail(email);
        this.senhaHash = senhaHash;
        if (cursoIds != null) {
            this.cursoIds.addAll(cursoIds);
        }
    }

    public abstract TipoUsuario tipo();

    public void definirNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new BusinessRuleException("Informe o nome");
        }
        this.nome = nome.trim();
    }

    public void definirEmail(String email) {
        this.email = normalizarEmail(email);
    }

    public void definirSenhaHash(String senhaHash) {
        if (senhaHash == null || senhaHash.isBlank()) {
            throw new BusinessRuleException("Senha inválida");
        }
        this.senhaHash = senhaHash;
    }

    public void exigirNovaSenha() {
        this.deveDefinirSenha = true;
    }

    public void concluirDefinicaoSenha(boolean deveDefinirSenha) {
        this.deveDefinirSenha = deveDefinirSenha;
    }

    public void definirAcessoExpiraEm(LocalDate acessoExpiraEm) {
        this.acessoExpiraEm = acessoExpiraEm;
    }

    public boolean acessoExpirado(LocalDate hoje) {
        return acessoExpiraEm != null && hoje.isAfter(acessoExpiraEm);
    }

    public void definirId(UUID id) {
        this.id = id;
    }

    public boolean inscritoOuMinistra(UUID cursoId) {
        return cursoIds.contains(cursoId);
    }

    public void adicionarCurso(UUID cursoId) {
        if (cursoId != null && !cursoIds.contains(cursoId)) {
            cursoIds.add(cursoId);
        }
    }

    public void substituirCursos(List<UUID> ids) {
        cursoIds.clear();
        if (ids != null) {
            cursoIds.addAll(ids.stream().filter(Objects::nonNull).distinct().toList());
        }
    }

    public UUID id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public String email() {
        return email;
    }

    public String senhaHash() {
        return senhaHash;
    }

    public boolean deveDefinirSenha() {
        return deveDefinirSenha;
    }

    public LocalDate acessoExpiraEm() {
        return acessoExpiraEm;
    }

    public List<UUID> cursoIds() {
        return Collections.unmodifiableList(cursoIds);
    }

    private static String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}
