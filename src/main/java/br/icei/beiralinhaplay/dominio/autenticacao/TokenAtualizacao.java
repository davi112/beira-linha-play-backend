package br.icei.beiralinhaplay.dominio.autenticacao;

import java.time.Instant;
import java.util.UUID;

public class TokenAtualizacao {

    private UUID id;
    private UUID usuarioId;
    private String tokenHash;
    private Instant expiraEm;
    private boolean revogado;

    public TokenAtualizacao(UUID id, UUID usuarioId, String tokenHash, Instant expiraEm, boolean revogado) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.tokenHash = tokenHash;
        this.expiraEm = expiraEm;
        this.revogado = revogado;
    }

    public boolean valido(Instant agora) {
        return !revogado && agora.isBefore(expiraEm);
    }

    public void revogar() {
        this.revogado = true;
    }

    public void definirId(UUID id) {
        this.id = id;
    }

    public UUID id() {
        return id;
    }

    public UUID usuarioId() {
        return usuarioId;
    }

    public String tokenHash() {
        return tokenHash;
    }

    public Instant expiraEm() {
        return expiraEm;
    }

    public boolean revogado() {
        return revogado;
    }
}
