package br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "monitor")
@DiscriminatorValue("MONITOR")
public class MonitorEntity extends UsuarioEntity {

    @Column(name = "curso_origem", length = 120)
    private String cursoOrigem;

    @ManyToMany
    @JoinTable(
            name = "monitoria_curso",
            joinColumns = @JoinColumn(name = "monitor_id"),
            inverseJoinColumns = @JoinColumn(name = "curso_id")
    )
    private Set<CursoEntity> cursos = new HashSet<>();

    public String getCursoOrigem() {
        return cursoOrigem;
    }

    public void setCursoOrigem(String cursoOrigem) {
        this.cursoOrigem = cursoOrigem;
    }

    public Set<CursoEntity> getCursos() {
        return cursos;
    }

    public void setCursos(Set<CursoEntity> cursos) {
        this.cursos = cursos;
    }
}
