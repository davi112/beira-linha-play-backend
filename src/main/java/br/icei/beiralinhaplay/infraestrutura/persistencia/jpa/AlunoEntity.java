package br.icei.beiralinhaplay.infraestrutura.persistencia.jpa;

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
@Table(name = "aluno")
@DiscriminatorValue("ALUNO")
public class AlunoEntity extends UsuarioEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String apelido;

    @Column(nullable = false)
    private int pontos;

    @Column(name = "imagem_perfil", columnDefinition = "TEXT")
    private String imagemPerfil;

    @ManyToMany
    @JoinTable(
            name = "inscricao_curso",
            joinColumns = @JoinColumn(name = "aluno_id"),
            inverseJoinColumns = @JoinColumn(name = "curso_id")
    )
    private Set<CursoEntity> cursos = new HashSet<>();

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public int getPontos() {
        return pontos;
    }

    public void setPontos(int pontos) {
        this.pontos = pontos;
    }

    public String getImagemPerfil() {
        return imagemPerfil;
    }

    public void setImagemPerfil(String imagemPerfil) {
        this.imagemPerfil = imagemPerfil;
    }

    public Set<CursoEntity> getCursos() {
        return cursos;
    }

    public void setCursos(Set<CursoEntity> cursos) {
        this.cursos = cursos;
    }
}
