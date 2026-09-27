package br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "log_importacao")
public class LogImportacaoEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    @Column(name = "nome_evento", nullable = false, length = 200)
    private String nomeEvento;

    @Column(name = "url_evento", length = 500)
    private String urlEvento;

    @Column(name = "quantidade_alunos", nullable = false)
    private int quantidadeAlunos;

    @Column(name = "quantidade_cursos", nullable = false)
    private int quantidadeCursos;

    @Column(name = "data_importacao", nullable = false)
    private Instant dataImportacao;

    @Column(name = "admin_id", nullable = false)
    private UUID adminId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNomeEvento() {
        return nomeEvento;
    }

    public void setNomeEvento(String nomeEvento) {
        this.nomeEvento = nomeEvento;
    }

    public String getUrlEvento() {
        return urlEvento;
    }

    public void setUrlEvento(String urlEvento) {
        this.urlEvento = urlEvento;
    }

    public int getQuantidadeAlunos() {
        return quantidadeAlunos;
    }

    public void setQuantidadeAlunos(int quantidadeAlunos) {
        this.quantidadeAlunos = quantidadeAlunos;
    }

    public int getQuantidadeCursos() {
        return quantidadeCursos;
    }

    public void setQuantidadeCursos(int quantidadeCursos) {
        this.quantidadeCursos = quantidadeCursos;
    }

    public Instant getDataImportacao() {
        return dataImportacao;
    }

    public void setDataImportacao(Instant dataImportacao) {
        this.dataImportacao = dataImportacao;
    }

    public UUID getAdminId() {
        return adminId;
    }

    public void setAdminId(UUID adminId) {
        this.adminId = adminId;
    }
}
