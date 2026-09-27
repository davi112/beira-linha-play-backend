package br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.repositorios;

import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.LogImportacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LogImportacaoJpaRepository extends JpaRepository<LogImportacaoEntity, UUID> {

    List<LogImportacaoEntity> findAllByOrderByDataImportacaoDesc();
}
