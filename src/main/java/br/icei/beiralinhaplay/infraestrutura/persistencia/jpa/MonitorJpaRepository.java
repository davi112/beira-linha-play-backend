package br.icei.beiralinhaplay.infraestrutura.persistencia.jpa;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonitorJpaRepository extends JpaRepository<MonitorEntity, UUID> {

    @EntityGraph(attributePaths = "cursos")
    Optional<MonitorEntity> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "cursos")
    @Query("select m from MonitorEntity m where m.id = :id")
    Optional<MonitorEntity> buscarCompleto(UUID id);

    @Query("select distinct m from MonitorEntity m left join fetch m.cursos")
    List<MonitorEntity> listarTodos();

    @Query("select count(m) > 0 from MonitorEntity m where lower(m.email) = lower(:email) and (:id is null or m.id <> :id)")
    boolean existsEmail(String email, UUID id);
}
