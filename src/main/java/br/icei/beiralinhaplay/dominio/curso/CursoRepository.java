package br.icei.beiralinhaplay.dominio.curso;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CursoRepository {

    Curso salvar(Curso curso);

    Optional<Curso> buscarPorId(UUID id);

    List<Curso> listar();

    void excluir(UUID id);

    boolean existeCodigoAcesso(String codigo, UUID ignorarId);
}
