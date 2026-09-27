package br.icei.beiralinhaplay.apresentacao.dto;

import java.time.Instant;

public record LogImportacaoResponse(
        String id,
        String nomeEvento,
        String urlEvento,
        int quantidadeAlunos,
        int quantidadeCursos,
        Instant dataImportacao,
        String adminNome
) {
}
