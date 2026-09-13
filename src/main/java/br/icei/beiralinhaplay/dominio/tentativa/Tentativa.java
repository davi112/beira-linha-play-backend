package br.icei.beiralinhaplay.dominio.tentativa;

import br.icei.beiralinhaplay.dominio.atividade.Atividade;
import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;
import br.icei.beiralinhaplay.dominio.compartilhado.DomainRules;
import br.icei.beiralinhaplay.dominio.questao.Questao;
import br.icei.beiralinhaplay.dominio.resposta.Resposta;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Tentativa {

    private UUID id;
    private Instant dataEnvio;
    private int pontuacaoObtida;
    private UUID alunoId;
    private UUID atividadeId;
    private final List<Resposta> respostas = new ArrayList<>();

    public Tentativa(
            UUID id,
            Instant dataEnvio,
            int pontuacaoObtida,
            UUID alunoId,
            UUID atividadeId,
            List<Resposta> respostas
    ) {
        this.id = id;
        this.dataEnvio = dataEnvio;
        this.pontuacaoObtida = pontuacaoObtida;
        this.alunoId = alunoId;
        this.atividadeId = atividadeId;
        if (respostas != null) {
            this.respostas.addAll(respostas);
        }
    }

    public static Tentativa corrigir(
            UUID alunoId,
            Atividade atividade,
            Map<UUID, UUID> alternativaPorQuestao,
            Instant agora,
            int tentativasJaUsadas,
            int melhorPontuacaoAnterior
    ) {
        if (tentativasJaUsadas >= DomainRules.MAX_TENTATIVAS) {
            throw new BusinessRuleException("Máximo de 2 tentativas por atividade");
        }
        if (atividade.concluida(tentativasJaUsadas, melhorPontuacaoAnterior)) {
            throw new BusinessRuleException("Atividade já concluída");
        }
        if (alternativaPorQuestao == null || alternativaPorQuestao.size() != atividade.quantQuestoes()) {
            throw new BusinessRuleException("Responda todas as questões da atividade");
        }

        List<Resposta> corrigidas = new ArrayList<>();
        int pontuacao = 0;
        for (Questao questao : atividade.questoes()) {
            UUID alternativaId = alternativaPorQuestao.get(questao.id());
            if (alternativaId == null) {
                throw new BusinessRuleException("Faltou responder a questão " + questao.id());
            }
            boolean correta = questao.alternativaPorId(alternativaId).correta();
            if (correta) {
                pontuacao += questao.valor();
            }
            corrigidas.add(new Resposta(null, correta, questao.id(), alternativaId));
        }

        return new Tentativa(null, agora, pontuacao, alunoId, atividade.id(), corrigidas);
    }

    public int pontosDelta(int melhorPontuacaoAnterior) {
        return Math.max(0, pontuacaoObtida - melhorPontuacaoAnterior);
    }

    public void definirId(UUID id) {
        this.id = id;
    }

    public UUID id() {
        return id;
    }

    public Instant dataEnvio() {
        return dataEnvio;
    }

    public int pontuacaoObtida() {
        return pontuacaoObtida;
    }

    public UUID alunoId() {
        return alunoId;
    }

    public UUID atividadeId() {
        return atividadeId;
    }

    public List<Resposta> respostas() {
        return Collections.unmodifiableList(respostas);
    }
}
