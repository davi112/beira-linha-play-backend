package br.icei.beiralinhaplay.apresentacao.dto;

import br.icei.beiralinhaplay.aplicacao.tentativa.MonitoramentoReport;

import java.util.List;

public record MonitoramentoResponse(
        int tamanhoTurma,
        int envios,
        int xpTotal,
        int mediaPontuacao,
        int mediaAcertoPercentual,
        List<MonitoramentoReport.EstatisticaQuestao> questoes,
        List<LinhaAlunoResposta> alunos
) {
    public record LinhaAlunoResposta(
            String alunoId,
            String nome,
            String apelido,
            int pontos,
            String imagemPerfil,
            TentativaResponse tentativa,
            List<String> respostasPorQuestao
    ) {
    }

    public static MonitoramentoResponse de(MonitoramentoReport relatorio) {
        return new MonitoramentoResponse(
                relatorio.tamanhoTurma(),
                relatorio.envios(),
                relatorio.xpTotal(),
                relatorio.mediaPontuacao(),
                relatorio.mediaAcertoPercentual(),
                relatorio.questoes(),
                relatorio.alunos().stream()
                        .map(linha -> new LinhaAlunoResposta(
                                DtoConverter.id(linha.alunoId()),
                                linha.nome(),
                                linha.apelido(),
                                linha.pontos(),
                                linha.imagemPerfil(),
                                linha.tentativa() == null ? null : DtoConverter.tentativa(linha.tentativa()),
                                linha.respostasPorQuestao()
                        ))
                        .toList()
        );
    }
}
