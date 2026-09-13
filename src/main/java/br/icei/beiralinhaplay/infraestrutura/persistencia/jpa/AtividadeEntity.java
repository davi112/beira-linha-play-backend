package br.icei.beiralinhaplay.infraestrutura.persistencia.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "atividade")
public class AtividadeEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    @Column(nullable = false, length = 180)
    private String titulo;

    @Column(name = "quant_questoes", nullable = false)
    private int quantQuestoes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modulo_id")
    private ModuloEntity modulo;

    @OneToMany(mappedBy = "atividade", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<QuestaoEntity> questoes = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getQuantQuestoes() {
        return quantQuestoes;
    }

    public void setQuantQuestoes(int quantQuestoes) {
        this.quantQuestoes = quantQuestoes;
    }

    public ModuloEntity getModulo() {
        return modulo;
    }

    public void setModulo(ModuloEntity modulo) {
        this.modulo = modulo;
    }

    public List<QuestaoEntity> getQuestoes() {
        return questoes;
    }
}
