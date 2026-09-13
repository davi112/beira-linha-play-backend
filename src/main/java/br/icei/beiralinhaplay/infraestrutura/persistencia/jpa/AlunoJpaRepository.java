package br.icei.beiralinhaplay.infraestrutura.persistencia.jpa;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlunoJpaRepository extends JpaRepository<AlunoEntity, UUID> {

    @EntityGraph(attributePaths = "cursos")
    Optional<AlunoEntity> findByApelidoIgnoreCase(String apelido);

    @EntityGraph(attributePaths = "cursos")
    @Query("select a from AlunoEntity a where a.id = :id")
    Optional<AlunoEntity> buscarCompleto(UUID id);

    @Query("select count(a) > 0 from AlunoEntity a where lower(a.apelido) = lower(:apelido) and (:id is null or a.id <> :id)")
    boolean existsApelido(String apelido, UUID id);

    @Query("select distinct a from AlunoEntity a left join fetch a.cursos")
    List<AlunoEntity> listarTodos();

    @Query("select distinct a from AlunoEntity a join a.cursos c where c.id = :cursoId")
    List<AlunoEntity> findByCursoId(UUID cursoId);
}
