package br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.repositorios;

import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AdminJpaRepository extends JpaRepository<AdminEntity, UUID> {

    Optional<AdminEntity> findByNomeIgnoreCase(String nome);

    @Query("select count(a) > 0 from AdminEntity a where lower(a.nome) = lower(:nome) and (:id is null or a.id <> :id)")
    boolean existsNome(String nome, UUID id);
}
