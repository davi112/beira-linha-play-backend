package br.icei.beiralinhaplay.apresentacao.dto;

import java.util.List;

public record ModuloResponse(
        String id,
        String nome,
        String cursoId,
        List<AtividadeResumo> atividades
) {
    public record AtividadeResumo(String id, String titulo, int quantQuestoes, String moduloId) {
    }
}
