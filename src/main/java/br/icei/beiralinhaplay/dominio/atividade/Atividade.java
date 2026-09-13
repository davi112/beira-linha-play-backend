package br.icei.beiralinhaplay.dominio.atividade;

import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;
import br.icei.beiralinhaplay.dominio.compartilhado.DomainRules;
import br.icei.beiralinhaplay.dominio.questao.Questao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Atividade {

    private UUID id;
    private String titulo;
    private UUID moduloId;
    private final List<Questao> questoes = new ArrayList<>();

    public Atividade(UUID id, String titulo, UUID moduloId, List<Questao> questoes) {
        definirTitulo(titulo);
        if (moduloId == null) {
            throw new BusinessRuleException("Atividade precisa de um módulo");
        }
        this.id = id;
        this.moduloId = moduloId;
        definirQuestoes(questoes);
    }

    public void definirTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new BusinessRuleException("Informe o título da atividade");
        }
        this.titulo = titulo.trim();
    }

    public void definirQuestoes(List<Questao> questoes) {
        if (questoes == null || questoes.isEmpty()) {
            throw new BusinessRuleException("A atividade precisa de pelo menos uma questão");
        }
        this.questoes.clear();
        this.questoes.addAll(questoes);
    }

    public int quantQuestoes() {
        return questoes.size();
    }

    public int xpTotal() {
        return questoes.stream().mapToInt(Questao::valor).sum();
    }

    public Questao questaoPorId(UUID questaoId) {
        return questoes.stream()
                .filter(questao -> questao.id().equals(questaoId))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("Questão não pertence à atividade"));
    }

    public boolean concluida(int tentativasUsadas, int melhorPontuacao) {
        return tentativasUsadas >= DomainRules.MAX_TENTATIVAS || melhorPontuacao >= xpTotal();
    }

    public void definirId(UUID id) {
        this.id = id;
    }

    public UUID id() {
        return id;
    }

    public String titulo() {
        return titulo;
    }

    public UUID moduloId() {
        return moduloId;
    }

    public List<Questao> questoes() {
        return Collections.unmodifiableList(questoes);
    }
}
