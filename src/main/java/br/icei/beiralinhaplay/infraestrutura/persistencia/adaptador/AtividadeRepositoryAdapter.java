package br.icei.beiralinhaplay.infraestrutura.persistencia.adaptador;

import br.icei.beiralinhaplay.dominio.atividade.Atividade;
import br.icei.beiralinhaplay.dominio.atividade.AtividadeRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.AtividadeEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.AtividadeJpaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.ModuloEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.ModuloJpaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.TentativaJpaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.mapeamento.ConteudoMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public class AtividadeRepositoryAdapter implements AtividadeRepository {

    private final AtividadeJpaRepository atividadeJpaRepository;
    private final ModuloJpaRepository moduloJpaRepository;
    private final TentativaJpaRepository tentativaJpaRepository;

    public AtividadeRepositoryAdapter(
            AtividadeJpaRepository atividadeJpaRepository,
            ModuloJpaRepository moduloJpaRepository,
            TentativaJpaRepository tentativaJpaRepository
    ) {
        this.atividadeJpaRepository = atividadeJpaRepository;
        this.moduloJpaRepository = moduloJpaRepository;
        this.tentativaJpaRepository = tentativaJpaRepository;
    }

    @Override
    public Atividade salvar(Atividade atividade) {
        AtividadeEntity jpa = atividade.id() == null
                ? new AtividadeEntity()
                : atividadeJpaRepository.buscarCompleto(atividade.id()).orElse(new AtividadeEntity());
        jpa.setTitulo(atividade.titulo());
        if (jpa.getModulo() == null) {
            ModuloEntity modulo = moduloJpaRepository.getReferenceById(atividade.moduloId());
            jpa.setModulo(modulo);
        }
        ConteudoMapper.copiarQuestoes(atividade, jpa);
        AtividadeEntity salvo = atividadeJpaRepository.save(jpa);
        return atividadeJpaRepository.buscarCompleto(salvo.getId())
                .map(ConteudoMapper::paraDominio)
                .orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Atividade> buscarPorId(UUID id) {
        return atividadeJpaRepository.buscarCompleto(id).map(ConteudoMapper::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Atividade> listarPorModulo(UUID moduloId) {
        return atividadeJpaRepository.findByModuloIdOrderByIdAsc(moduloId).stream()
                .map(ConteudoMapper::paraDominio)
                .toList();
    }

    @Override
    public void excluir(UUID id) {
        atividadeJpaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean possuiTentativas(UUID atividadeId) {
        return tentativaJpaRepository.existsByAtividadeId(atividadeId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean possuiTentativasNoModulo(UUID moduloId) {
        return tentativaJpaRepository.existsByAtividade_Modulo_Id(moduloId);
    }
}
