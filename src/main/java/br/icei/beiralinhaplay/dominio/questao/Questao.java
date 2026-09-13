package br.icei.beiralinhaplay.dominio.questao;

import br.icei.beiralinhaplay.dominio.alternativa.Alternativa;
import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;
import br.icei.beiralinhaplay.dominio.compartilhado.DomainRules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Questao {

    private UUID id;
    private String enunciado;
    private int valor;
    private final List<Alternativa> alternativas = new ArrayList<>();

    public Questao(UUID id, String enunciado, int valor, List<Alternativa> alternativas) {
        definirEnunciado(enunciado);
        definirValor(valor);
        this.id = id;
        definirAlternativas(alternativas);
    }

    public void definirEnunciado(String enunciado) {
        if (enunciado == null || enunciado.isBlank()) {
            throw new BusinessRuleException("Informe o enunciado da questão");
        }
        this.enunciado = enunciado.trim();
    }

    public void definirValor(int valor) {
        if (valor <= 0) {
            throw new BusinessRuleException("O valor da questão deve ser positivo");
        }
        this.valor = valor;
    }

    public void definirAlternativas(List<Alternativa> alternativas) {
        if (alternativas == null
                || alternativas.size() < DomainRules.MIN_ALTERNATIVAS
                || alternativas.size() > DomainRules.MAX_ALTERNATIVAS) {
            throw new BusinessRuleException("Cada questão deve ter de 2 a 4 alternativas");
        }
        long corretas = alternativas.stream().filter(Alternativa::correta).count();
        if (corretas != 1) {
            throw new BusinessRuleException("Cada questão deve ter exatamente uma alternativa correta");
        }
        this.alternativas.clear();
        this.alternativas.addAll(alternativas);
    }

    public Alternativa alternativaPorId(UUID alternativaId) {
        return alternativas.stream()
                .filter(alternativa -> alternativa.id().equals(alternativaId))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("Alternativa não pertence à questão"));
    }

    public void definirId(UUID id) {
        this.id = id;
    }

    public UUID id() {
        return id;
    }

    public String enunciado() {
        return enunciado;
    }

    public int valor() {
        return valor;
    }

    public List<Alternativa> alternativas() {
        return Collections.unmodifiableList(alternativas);
    }
}
