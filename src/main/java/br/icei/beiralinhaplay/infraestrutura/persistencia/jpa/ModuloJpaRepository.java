package br.icei.beiralinhaplay.infraestrutura.persistencia.jpa;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ModuloJpaRepository extends JpaRepository<ModuloEntity, UUID> {

    @EntityGraph(attributePaths = "atividades")
    List<ModuloEntity> findByCursoIdOrderByIdAsc(UUID cursoId);

    @EntityGraph(attributePaths = {"atividades", "curso"})
    @Query("select m from ModuloEntity m where m.id = :id")
    Optional<ModuloEntity> buscarCompleto(UUID id);
}
