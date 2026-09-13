package br.icei.beiralinhaplay.aplicacao.tentativa;

import java.util.Map;
import java.util.UUID;

public record EnviarTentativaCommand(Map<UUID, UUID> alternativaPorQuestao) {
}
