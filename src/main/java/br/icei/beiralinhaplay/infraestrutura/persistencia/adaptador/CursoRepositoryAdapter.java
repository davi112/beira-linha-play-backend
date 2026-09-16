package br.icei.beiralinhaplay.infraestrutura.persistencia.adaptador;

import br.icei.beiralinhaplay.dominio.curso.Curso;
import br.icei.beiralinhaplay.dominio.curso.CursoRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.CursoEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.CursoJpaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.MonitorEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.MonitorJpaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.mapeamento.ConteudoMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@Transactional
public class CursoRepositoryAdapter implements CursoRepository {

    private final CursoJpaRepository cursoJpaRepository;
    private final MonitorJpaRepository monitorJpaRepository;

    public CursoRepositoryAdapter(CursoJpaRepository cursoJpaRepository, MonitorJpaRepository monitorJpaRepository) {
        this.cursoJpaRepository = cursoJpaRepository;
        this.monitorJpaRepository = monitorJpaRepository;
    }

    @Override
    public Curso salvar(Curso curso) {
        CursoEntity jpa = curso.id() == null
                ? new CursoEntity()
                : cursoJpaRepository.buscarCompleto(curso.id()).orElse(new CursoEntity());
        jpa.setNome(curso.nome());
        jpa.setCodigoAcesso(curso.codigoAcesso());
        Set<MonitorEntity> monitores = new HashSet<>(monitorJpaRepository.findAllById(curso.monitorIds()));
        jpa.setMonitores(monitores);
        CursoEntity salvo = cursoJpaRepository.save(jpa);
        return cursoJpaRepository.buscarCompleto(salvo.getId())
                .map(ConteudoMapper::paraDominio)
                .orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Curso> buscarPorId(UUID id) {
        return cursoJpaRepository.buscarCompleto(id).map(ConteudoMapper::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Curso> listar() {
        return cursoJpaRepository.findAllComRelacoes().stream().map(ConteudoMapper::paraDominio).toList();
    }

    @Override
    public void excluir(UUID id) {
        cursoJpaRepository.buscarCompleto(id).ifPresent(cursoJpaRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCodigoAcesso(String codigo, UUID ignorarId) {
        if (ignorarId == null) {
            return cursoJpaRepository.existsByCodigoAcessoIgnoreCase(codigo);
        }
        return cursoJpaRepository.existsByCodigoAcessoIgnoreCaseAndIdNot(codigo, ignorarId);
    }
}
