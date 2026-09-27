package br.icei.beiralinhaplay.aplicacao.questoes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QuestaoGerada(
        String enunciado,
        int valor,
        List<AlternativaGerada> alternativas
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AlternativaGerada(String descricao, boolean correta) {
    }
}
