package br.icei.beiralinhaplay.dominio.medalha;

import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;

import java.util.UUID;

public class Medalha {

    private UUID id;
    private String nome;
    private String imagemUrl;
    private int pontosMin;

    public Medalha(UUID id, String nome, String imagemUrl, int pontosMin) {
        definirNome(nome);
        definirImagemUrl(imagemUrl);
        definirPontosMin(pontosMin);
        this.id = id;
    }

    public void definirNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new BusinessRuleException("Informe o nome da medalha");
        }
        this.nome = nome.trim();
    }

    public void definirImagemUrl(String imagemUrl) {
        if (imagemUrl == null || imagemUrl.isBlank()) {
            throw new BusinessRuleException("Informe a imagem da medalha");
        }
        this.imagemUrl = imagemUrl.trim();
    }

    public void definirPontosMin(int pontosMin) {
        if (pontosMin < 0) {
            throw new BusinessRuleException("pontosMin não pode ser negativo");
        }
        this.pontosMin = pontosMin;
    }

    public void definirId(UUID id) {
        this.id = id;
    }

    public UUID id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public String imagemUrl() {
        return imagemUrl;
    }

    public int pontosMin() {
        return pontosMin;
    }
}
