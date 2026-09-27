package br.icei.beiralinhaplay.dominio.importacao;

import java.time.Instant;
import java.util.UUID;

public record LogImportacao(
        UUID id,
        String nomeEvento,
        String urlEvento,
        int quantidadeAlunos,
        int quantidadeCursos,
        Instant dataImportacao,
        UUID adminId
) {
}
