package br.icei.beiralinhaplay.infraestrutura.persistencia.jpa;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CursoJpaRepository extends JpaRepository<CursoEntity, UUID> {

    @EntityGraph(attributePaths = {"monitores", "modulos"})
    @Query("select c from CursoEntity c")
    List<CursoEntity> findAllComRelacoes();

    @EntityGraph(attributePaths = {"monitores", "modulos"})
    @Query("select c from CursoEntity c where c.id = :id")
    Optional<CursoEntity> buscarCompleto(UUID id);

    boolean existsByCodigoAcessoIgnoreCaseAndIdNot(String codigo, UUID id);

    boolean existsByCodigoAcessoIgnoreCase(String codigo);

    @EntityGraph(attributePaths = {"monitores", "modulos"})
    Optional<CursoEntity> findByCodigoAcessoIgnoreCase(String codigo);
}
