package br.icei.beiralinhaplay.aplicacao.ranking;

import br.icei.beiralinhaplay.dominio.usuario.Aluno;

import java.util.UUID;

public record RankingPosition(
        int posicao,
        UUID id,
        String nome,
        String apelido,
        int pontos,
        String imagemPerfil
) {
    public static RankingPosition de(int posicao, Aluno aluno) {
        return new RankingPosition(posicao, aluno.id(), aluno.nome(), aluno.apelido(), aluno.pontos(), aluno.imagemPerfil());
    }
}
