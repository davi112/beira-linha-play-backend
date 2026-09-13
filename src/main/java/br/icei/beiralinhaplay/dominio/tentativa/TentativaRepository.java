package br.icei.beiralinhaplay.dominio.tentativa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TentativaRepository {

    Tentativa salvar(Tentativa tentativa);

    List<Tentativa> listarPorAlunoEAtividade(UUID alunoId, UUID atividadeId);

    List<Tentativa> listarPorAtividade(UUID atividadeId);

    List<Tentativa> listarPorAluno(UUID alunoId);

    Optional<Tentativa> buscarPorId(UUID id);
}
