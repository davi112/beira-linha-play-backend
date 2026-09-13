package br.icei.beiralinhaplay.dominio.modulo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ModuloRepository {

    Modulo salvar(Modulo modulo);

    Optional<Modulo> buscarPorId(UUID id);

    List<Modulo> listarPorCurso(UUID cursoId);

    void excluir(UUID id);
}
