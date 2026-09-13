package br.icei.beiralinhaplay.infraestrutura.persistencia.jpa;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TentativaJpaRepository extends JpaRepository<TentativaEntity, UUID> {

    @EntityGraph(attributePaths = {"respostas", "respostas.questao", "respostas.alternativa"})
    List<TentativaEntity> findByAlunoIdAndAtividadeIdOrderByDataEnvioAsc(UUID alunoId, UUID atividadeId);

    @EntityGraph(attributePaths = {"respostas", "respostas.questao", "respostas.alternativa", "aluno"})
    List<TentativaEntity> findByAtividadeIdOrderByDataEnvioAsc(UUID atividadeId);

    @EntityGraph(attributePaths = {"respostas"})
    List<TentativaEntity> findByAlunoIdOrderByDataEnvioDesc(UUID alunoId);

    boolean existsByAtividadeId(UUID atividadeId);

    @EntityGraph(attributePaths = {"respostas", "respostas.questao", "respostas.alternativa", "aluno", "atividade"})
    @Query("select t from TentativaEntity t where t.id = :id")
    Optional<TentativaEntity> buscarCompleto(UUID id);
}
