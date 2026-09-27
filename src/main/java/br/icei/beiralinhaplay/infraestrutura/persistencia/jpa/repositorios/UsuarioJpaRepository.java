package br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.repositorios;

import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {

    List<UsuarioEntity> findAllByEmailIgnoreCase(String email);

    @Query("""
            select u from UsuarioEntity u
            where u.id = :id
              and (u.acessoExpiraEm is null or u.acessoExpiraEm >= :hoje)
            """)
    Optional<UsuarioEntity> buscarNaoExpirado(UUID id, LocalDate hoje);
}
