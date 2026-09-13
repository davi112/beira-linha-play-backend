package br.icei.beiralinhaplay.infraestrutura.persistencia.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlternativaJpaRepository extends JpaRepository<AlternativaEntity, UUID> {
}
