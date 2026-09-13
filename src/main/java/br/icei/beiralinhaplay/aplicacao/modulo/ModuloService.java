package br.icei.beiralinhaplay.aplicacao.modulo;

import br.icei.beiralinhaplay.dominio.compartilhado.ForbiddenException;
import br.icei.beiralinhaplay.dominio.compartilhado.ResourceNotFoundException;
import br.icei.beiralinhaplay.dominio.curso.Curso;
import br.icei.beiralinhaplay.dominio.curso.CursoRepository;
import br.icei.beiralinhaplay.dominio.modulo.Modulo;
import br.icei.beiralinhaplay.dominio.modulo.ModuloRepository;
import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;

import java.util.List;
import java.util.UUID;

public class ModuloService {

    private final ModuloRepository repositorioModulo;
    private final CursoRepository repositorioCurso;

    public ModuloService(ModuloRepository repositorioModulo, CursoRepository repositorioCurso) {
        this.repositorioModulo = repositorioModulo;
        this.repositorioCurso = repositorioCurso;
    }

    public List<Modulo> listarPorCurso(UUID cursoId) {
        return repositorioModulo.listarPorCurso(cursoId);
    }

    public Modulo buscar(UUID id) {
        return repositorioModulo.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo não encontrado"));
    }

    public Modulo criar(Usuario solicitante, UUID cursoId, SalvarModuloCommand comando) {
        Curso curso = repositorioCurso.buscarPorId(cursoId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado"));
        exigirMonitorDoCurso(solicitante, curso);
        Modulo modulo = new Modulo(null, comando.nome(), cursoId, List.of());
        return repositorioModulo.salvar(modulo);
    }

    public Modulo atualizar(Usuario solicitante, UUID id, SalvarModuloCommand comando) {
        Modulo modulo = buscar(id);
        Curso curso = repositorioCurso.buscarPorId(modulo.cursoId())
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado"));
        exigirMonitorDoCurso(solicitante, curso);
        modulo.definirNome(comando.nome());
        return repositorioModulo.salvar(modulo);
    }

    public void excluir(Usuario solicitante, UUID id) {
        Modulo modulo = buscar(id);
        Curso curso = repositorioCurso.buscarPorId(modulo.cursoId())
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado"));
        exigirMonitorDoCurso(solicitante, curso);
        repositorioModulo.excluir(id);
    }

    private static void exigirMonitorDoCurso(Usuario solicitante, Curso curso) {
        if (solicitante.tipo() == TipoUsuario.ADMIN) {
            return;
        }
        if (solicitante.tipo() != TipoUsuario.MONITOR || !curso.monitorIds().contains(solicitante.id())) {
            throw new ForbiddenException("Apenas o monitor do curso pode alterar módulos");
        }
    }
}
