package br.icei.beiralinhaplay.apresentacao.dto;

import java.util.List;

public record CursoResponse(
        String id,
        String nome,
        String codigoAcesso,
        List<String> monitorIds,
        List<String> monitorNomes,
        List<ModuloResumo> modulos
) {
    public record ModuloResumo(String id, String nome, String cursoId) {
    }
}
