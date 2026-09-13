package br.icei.beiralinhaplay.aplicacao.curso;

import java.util.List;
import java.util.UUID;

public record SalvarCursoCommand(String nome, List<UUID> monitorIds) {
}
