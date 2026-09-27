package br.icei.beiralinhaplay.infraestrutura.persistencia.adaptador;

import br.icei.beiralinhaplay.dominio.modulo.Modulo;
import br.icei.beiralinhaplay.dominio.modulo.ModuloRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.CursoEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.repositorios.CursoJpaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.ModuloEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.repositorios.ModuloJpaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.mapeamento.ConteudoMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public class ModuloRepositoryAdapter implements ModuloRepository {

    private final ModuloJpaRepository moduloJpaRepository;
    private final CursoJpaRepository cursoJpaRepository;

    public ModuloRepositoryAdapter(ModuloJpaRepository moduloJpaRepository, CursoJpaRepository cursoJpaRepository) {
        this.moduloJpaRepository = moduloJpaRepository;
        this.cursoJpaRepository = cursoJpaRepository;
    }

    @Override
    public Modulo salvar(Modulo modulo) {
        ModuloEntity jpa = modulo.id() == null
                ? new ModuloEntity()
                : moduloJpaRepository.buscarCompleto(modulo.id()).orElse(new ModuloEntity());
        jpa.setNome(modulo.nome());
        if (jpa.getCurso() == null) {
            CursoEntity curso = cursoJpaRepository.getReferenceById(modulo.cursoId());
            jpa.setCurso(curso);
        }
        ModuloEntity salvo = moduloJpaRepository.save(jpa);
        return moduloJpaRepository.buscarCompleto(salvo.getId())
                .map(ConteudoMapper::paraDominio)
                .orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Modulo> buscarPorId(UUID id) {
        return moduloJpaRepository.buscarCompleto(id).map(ConteudoMapper::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Modulo> listarPorCurso(UUID cursoId) {
        return moduloJpaRepository.findByCursoIdOrderByIdAsc(cursoId).stream()
                .map(ConteudoMapper::paraDominio)
                .toList();
    }

    @Override
    public void excluir(UUID id) {
        moduloJpaRepository.buscarCompleto(id).ifPresent(moduloJpaRepository::delete);
    }
}
