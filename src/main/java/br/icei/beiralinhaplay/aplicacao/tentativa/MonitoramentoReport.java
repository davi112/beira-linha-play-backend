package br.icei.beiralinhaplay.aplicacao.tentativa;

import br.icei.beiralinhaplay.dominio.alternativa.Alternativa;
import br.icei.beiralinhaplay.dominio.atividade.Atividade;
import br.icei.beiralinhaplay.dominio.questao.Questao;
import br.icei.beiralinhaplay.dominio.resposta.Resposta;
import br.icei.beiralinhaplay.dominio.tentativa.Tentativa;
import br.icei.beiralinhaplay.dominio.usuario.Aluno;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record MonitoramentoReport(
        int tamanhoTurma,
        int envios,
        int xpTotal,
        int mediaPontuacao,
        int mediaAcertoPercentual,
        List<EstatisticaQuestao> questoes,
        List<LinhaAluno> alunos
) {
    public record EstatisticaAlternativa(UUID alternativaId, String descricao, boolean correta, String letra, int votos, int percentual, boolean distrator) {
    }

    public record EstatisticaQuestao(UUID questaoId, String enunciado, int numero, int acertoPercentual, List<EstatisticaAlternativa> alternativas) {
    }

    public record LinhaAluno(
            UUID alunoId,
            String nome,
            String apelido,
            int pontos,
            String imagemPerfil,
            Tentativa tentativa,
            List<String> respostasPorQuestao
    ) {
    }

    public static MonitoramentoReport calcular(Atividade atividade, List<Aluno> turma, List<Tentativa> ultimas) {
        int envios = ultimas.size();
        int xpTotal = atividade.xpTotal();
        int mediaPontuacao = envios == 0
                ? 0
                : (int) Math.round(ultimas.stream().mapToInt(Tentativa::pontuacaoObtida).average().orElse(0));

        int mediaAcerto = envios == 0
                ? 0
                : (int) Math.round(ultimas.stream().mapToDouble(t -> {
                    long corretas = t.respostas().stream().filter(Resposta::correta).count();
                    return (double) corretas / Math.max(atividade.quantQuestoes(), 1);
                }).average().orElse(0) * 100);

        List<EstatisticaQuestao> stats = new ArrayList<>();
        for (int i = 0; i < atividade.questoes().size(); i++) {
            Questao questao = atividade.questoes().get(i);
            int[] votos = new int[questao.alternativas().size()];
            for (int a = 0; a < questao.alternativas().size(); a++) {
                UUID altId = questao.alternativas().get(a).id();
                votos[a] = (int) ultimas.stream()
                        .filter(t -> t.respostas().stream().anyMatch(r -> r.alternativaId().equals(altId)))
                        .count();
            }
            int corretas = 0;
            int maxErradas = 0;
            for (int a = 0; a < questao.alternativas().size(); a++) {
                Alternativa alt = questao.alternativas().get(a);
                if (alt.correta()) {
                    corretas += votos[a];
                } else {
                    maxErradas = Math.max(maxErradas, votos[a]);
                }
            }
            int acerto = envios == 0 ? 0 : Math.round(corretas * 100f / envios);
            List<EstatisticaAlternativa> alts = new ArrayList<>();
            for (int a = 0; a < questao.alternativas().size(); a++) {
                Alternativa alt = questao.alternativas().get(a);
                int percent = envios == 0 ? 0 : Math.round(votos[a] * 100f / envios);
                boolean distrator = !alt.correta() && votos[a] == maxErradas && maxErradas > 0;
                alts.add(new EstatisticaAlternativa(
                        alt.id(),
                        alt.descricao(),
                        alt.correta(),
                        String.valueOf((char) ('A' + a)),
                        votos[a],
                        percent,
                        distrator
                ));
            }
            stats.add(new EstatisticaQuestao(questao.id(), questao.enunciado(), i + 1, acerto, alts));
        }

        List<LinhaAluno> linhas = turma.stream().map(aluno -> {
            Tentativa tentativa = ultimas.stream()
                    .filter(t -> t.alunoId().equals(aluno.id()))
                    .findFirst()
                    .orElse(null);
            List<String> letras = atividade.questoes().stream().map(questao -> {
                if (tentativa == null) {
                    return null;
                }
                return tentativa.respostas().stream()
                        .filter(r -> r.questaoId().equals(questao.id()))
                        .findFirst()
                        .map(r -> {
                            int idx = -1;
                            for (int a = 0; a < questao.alternativas().size(); a++) {
                                if (questao.alternativas().get(a).id().equals(r.alternativaId())) {
                                    idx = a;
                                    break;
                                }
                            }
                            return idx >= 0 ? String.valueOf((char) ('A' + idx)) : null;
                        })
                        .orElse(null);
            }).toList();
            return new LinhaAluno(
                    aluno.id(),
                    aluno.nome(),
                    aluno.apelido(),
                    aluno.pontos(),
                    aluno.imagemPerfil(),
                    tentativa,
                    letras
            );
        }).toList();

        return new MonitoramentoReport(turma.size(), envios, xpTotal, mediaPontuacao, mediaAcerto, stats, linhas);
    }
}
