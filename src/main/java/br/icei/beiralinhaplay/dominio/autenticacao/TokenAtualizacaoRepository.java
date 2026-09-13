package br.icei.beiralinhaplay.dominio.autenticacao;

import java.util.Optional;
import java.util.UUID;

public interface TokenAtualizacaoRepository {

    TokenAtualizacao salvar(TokenAtualizacao token);

    Optional<TokenAtualizacao> buscarPorHash(String tokenHash);

    void revogarTodosDoUsuario(UUID usuarioId);
}
