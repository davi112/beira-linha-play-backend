package br.icei.beiralinhaplay.aplicacao.atividade;

import java.util.List;

public record SalvarAtividadeCommand(
        String titulo,
        List<QuestaoCommand> questoes
) {
    public record QuestaoCommand(String enunciado, int valor, List<AlternativaCommand> alternativas) {
    }

    public record AlternativaCommand(String descricao, boolean correta) {
    }
}
