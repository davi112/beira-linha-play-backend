package br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "resposta")
public class RespostaEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    @Column(nullable = false)
    private boolean correta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tentativa_id")
    private TentativaEntity tentativa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questao_id")
    private QuestaoEntity questao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alternativa_id")
    private AlternativaEntity alternativa;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public boolean isCorreta() {
        return correta;
    }

    public void setCorreta(boolean correta) {
        this.correta = correta;
    }

    public TentativaEntity getTentativa() {
        return tentativa;
    }

    public void setTentativa(TentativaEntity tentativa) {
        this.tentativa = tentativa;
    }

    public QuestaoEntity getQuestao() {
        return questao;
    }

    public void setQuestao(QuestaoEntity questao) {
        this.questao = questao;
    }

    public AlternativaEntity getAlternativa() {
        return alternativa;
    }

    public void setAlternativa(AlternativaEntity alternativa) {
        this.alternativa = alternativa;
    }
}
