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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tentativa")
public class TentativaEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    @Column(name = "data_envio", nullable = false)
    private Instant dataEnvio;

    @Column(name = "pontuacao_obtida", nullable = false)
    private int pontuacaoObtida;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id")
    private AlunoEntity aluno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atividade_id")
    private AtividadeEntity atividade;

    @OneToMany(mappedBy = "tentativa", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<RespostaEntity> respostas = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(Instant dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public int getPontuacaoObtida() {
        return pontuacaoObtida;
    }

    public void setPontuacaoObtida(int pontuacaoObtida) {
        this.pontuacaoObtida = pontuacaoObtida;
    }

    public AlunoEntity getAluno() {
        return aluno;
    }

    public void setAluno(AlunoEntity aluno) {
        this.aluno = aluno;
    }

    public AtividadeEntity getAtividade() {
        return atividade;
    }

    public void setAtividade(AtividadeEntity atividade) {
        this.atividade = atividade;
    }

    public List<RespostaEntity> getRespostas() {
        return respostas;
    }
}
