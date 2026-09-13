package br.icei.beiralinhaplay.dominio.atividade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AtividadeRepository {

    Atividade salvar(Atividade atividade);

    Optional<Atividade> buscarPorId(UUID id);

    List<Atividade> listarPorModulo(UUID moduloId);

    void excluir(UUID id);

    boolean possuiTentativas(UUID atividadeId);
}
