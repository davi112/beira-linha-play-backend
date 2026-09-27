package br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.repositorios;

import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.MedalhaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MedalhaJpaRepository extends JpaRepository<MedalhaEntity, UUID> {
}
