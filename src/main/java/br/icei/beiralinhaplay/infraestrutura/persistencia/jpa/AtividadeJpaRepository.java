package br.icei.beiralinhaplay.infraestrutura.persistencia.jpa;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AtividadeJpaRepository extends JpaRepository<AtividadeEntity, UUID> {

    @EntityGraph(attributePaths = {"questoes", "questoes.alternativas", "modulo"})
    List<AtividadeEntity> findByModuloIdOrderByIdAsc(UUID moduloId);

    @EntityGraph(attributePaths = {"questoes", "questoes.alternativas", "modulo"})
    @Query("select a from AtividadeEntity a where a.id = :id")
    Optional<AtividadeEntity> buscarCompleto(UUID id);
}
