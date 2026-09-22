package br.icei.beiralinhaplay.infraestrutura.persistencia.adaptador;

import br.icei.beiralinhaplay.dominio.autenticacao.TokenAtualizacaoRepository;
import br.icei.beiralinhaplay.dominio.autenticacao.TokenAtualizacao;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.TokenAtualizacaoEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.repositorios.TokenAtualizacaoJpaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.repositorios.UsuarioJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public class TokenAtualizacaoRepositoryAdapter implements TokenAtualizacaoRepository {

    private final TokenAtualizacaoJpaRepository tokenJpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;

    public TokenAtualizacaoRepositoryAdapter(
            TokenAtualizacaoJpaRepository tokenJpaRepository,
            UsuarioJpaRepository usuarioJpaRepository
    ) {
        this.tokenJpaRepository = tokenJpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    @Override
    public TokenAtualizacao salvar(TokenAtualizacao token) {
        TokenAtualizacaoEntity jpa = token.id() == null
                ? new TokenAtualizacaoEntity()
                : tokenJpaRepository.findById(token.id()).orElse(new TokenAtualizacaoEntity());
        if (jpa.getUsuario() == null) {
            jpa.setUsuario(usuarioJpaRepository.getReferenceById(token.usuarioId()));
        }
        jpa.setTokenHash(token.tokenHash());
        jpa.setExpiraEm(token.expiraEm());
        jpa.setRevogado(token.revogado());
        TokenAtualizacaoEntity salvo = tokenJpaRepository.save(jpa);
        token.definirId(salvo.getId());
        return token;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TokenAtualizacao> buscarPorHash(String tokenHash) {
        return tokenJpaRepository.buscarPorHash(tokenHash).map(jpa -> new TokenAtualizacao(
                jpa.getId(),
                jpa.getUsuario().getId(),
                jpa.getTokenHash(),
                jpa.getExpiraEm(),
                jpa.isRevogado()
        ));
    }

    @Override
    public void revogarTodosDoUsuario(UUID usuarioId) {
        tokenJpaRepository.revogarTodosDoUsuario(usuarioId);
    }
}
