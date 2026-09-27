package br.icei.beiralinhaplay.dominio.usuario;

import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;
import br.icei.beiralinhaplay.dominio.medalha.Medalha;

import java.util.List;
import java.util.UUID;

public class Aluno extends Usuario {

    private String apelido;
    private int pontos;
    private String imagemPerfil;
    private UUID logImportacaoId;

    public Aluno(
            UUID id,
            String nome,
            String email,
            String senhaHash,
            List<UUID> cursoIds,
            String apelido,
            int pontos,
            String imagemPerfil
    ) {
        super(id, nome, email, senhaHash, cursoIds);
        definirApelido(apelido);
        this.pontos = Math.max(pontos, 0);
        this.imagemPerfil = imagemPerfil == null ? "" : imagemPerfil;
    }

    @Override
    public TipoUsuario tipo() {
        return TipoUsuario.ALUNO;
    }

    public void definirApelido(String apelido) {
        if (apelido == null || apelido.isBlank()) {
            throw new BusinessRuleException("Informe o apelido");
        }
        this.apelido = apelido.trim();
    }

    public void adicionarPontos(int delta) {
        if (delta < 0) {
            throw new BusinessRuleException("O delta de pontos não pode ser negativo");
        }
        this.pontos += delta;
    }

    public void definirImagemPerfil(String imagemPerfil) {
        this.imagemPerfil = imagemPerfil == null ? "" : imagemPerfil;
    }

    public void definirLogImportacao(UUID logImportacaoId) {
        this.logImportacaoId = logImportacaoId;
    }

    public boolean conquistou(Medalha medalha) {
        return pontos >= medalha.pontosMin();
    }

    public String apelido() {
        return apelido;
    }

    public int pontos() {
        return pontos;
    }

    public String imagemPerfil() {
        return imagemPerfil;
    }

    public UUID logImportacaoId() {
        return logImportacaoId;
    }
}
