package br.icei.beiralinhaplay.infraestrutura.persistencia.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface TokenAtualizacaoJpaRepository extends JpaRepository<TokenAtualizacaoEntity, UUID> {

    Optional<TokenAtualizacaoEntity> findByTokenHash(String tokenHash);

    @Query("select t from TokenAtualizacaoEntity t join fetch t.usuario where t.tokenHash = :tokenHash")
    Optional<TokenAtualizacaoEntity> buscarPorHash(String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update TokenAtualizacaoEntity t set t.revogado = true where t.usuario.id = :usuarioId")
    void revogarTodosDoUsuario(UUID usuarioId);
}
