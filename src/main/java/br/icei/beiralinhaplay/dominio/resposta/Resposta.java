package br.icei.beiralinhaplay.dominio.resposta;

import java.util.UUID;

public class Resposta {

    private UUID id;
    private boolean correta;
    private UUID questaoId;
    private UUID alternativaId;

    public Resposta(UUID id, boolean correta, UUID questaoId, UUID alternativaId) {
        this.id = id;
        this.correta = correta;
        this.questaoId = questaoId;
        this.alternativaId = alternativaId;
    }

    public void definirId(UUID id) {
        this.id = id;
    }

    public UUID id() {
        return id;
    }

    public boolean correta() {
        return correta;
    }

    public UUID questaoId() {
        return questaoId;
    }

    public UUID alternativaId() {
        return alternativaId;
    }
}
