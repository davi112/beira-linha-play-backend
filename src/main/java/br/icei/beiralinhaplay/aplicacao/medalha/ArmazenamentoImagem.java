package br.icei.beiralinhaplay.aplicacao.medalha;

public interface ArmazenamentoImagem {

    String enviar(byte[] conteudo, String contentType, String nomeArquivo);

    void excluir(String imagemUrl);
}
