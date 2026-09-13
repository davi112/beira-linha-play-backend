package br.icei.beiralinhaplay.infraestrutura.persistencia.jpa;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin")
@DiscriminatorValue("ADMIN")
public class AdminEntity extends UsuarioEntity {
}
