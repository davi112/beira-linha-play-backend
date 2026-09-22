package br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.repositorios;

import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {
}
