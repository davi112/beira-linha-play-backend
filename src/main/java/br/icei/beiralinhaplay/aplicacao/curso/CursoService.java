package br.icei.beiralinhaplay.aplicacao.curso;

import br.icei.beiralinhaplay.dominio.compartilhado.ForbiddenException;
import br.icei.beiralinhaplay.dominio.compartilhado.ResourceNotFoundException;
import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;
import br.icei.beiralinhaplay.dominio.curso.Curso;
import br.icei.beiralinhaplay.dominio.curso.CursoRepository;
import br.icei.beiralinhaplay.dominio.usuario.Aluno;
import br.icei.beiralinhaplay.dominio.usuario.UsuarioRepository;
import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;

import java.util.List;
import java.util.UUID;

public class CursoService {

    private final CursoRepository repositorioCurso;
    private final UsuarioRepository repositorioUsuario;

    public CursoService(CursoRepository repositorioCurso, UsuarioRepository repositorioUsuario) {
        this.repositorioCurso = repositorioCurso;
        this.repositorioUsuario = repositorioUsuario;
    }

    public List<Curso> listar() {
        return repositorioCurso.listar();
    }

    public Curso buscar(UUID id) {
        return repositorioCurso.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado"));
    }

    public Curso criar(Usuario solicitante, SalvarCursoCommand comando) {
        exigirAdmin(solicitante);
        validarMonitores(comando.monitorIds());
        Curso curso = new Curso(null, comando.nome(), null, comando.monitorIds(), List.of());
        int tentativas = 0;
        while (repositorioCurso.existeCodigoAcesso(curso.codigoAcesso(), null) && tentativas < 8) {
            curso.regenerarCodigoAcesso();
            tentativas++;
        }
        if (repositorioCurso.existeCodigoAcesso(curso.codigoAcesso(), null)) {
            throw new BusinessRuleException("Não foi possível gerar um código de acesso único");
        }
        return repositorioCurso.salvar(curso);
    }

    public Curso atualizar(Usuario solicitante, UUID id, SalvarCursoCommand comando) {
        exigirAdmin(solicitante);
        Curso curso = buscar(id);
        curso.definirNome(comando.nome());
        curso.definirMonitores(comando.monitorIds());
        validarMonitores(comando.monitorIds());
        return repositorioCurso.salvar(curso);
    }

    public void excluir(Usuario solicitante, UUID id) {
        exigirAdmin(solicitante);
        buscar(id);
        repositorioCurso.excluir(id);
    }

    public Curso inscrever(Usuario solicitante, UUID cursoId, String codigoAcesso) {
        if (solicitante.tipo() != TipoUsuario.ALUNO) {
            throw new ForbiddenException("Apenas alunos entram com código de acesso");
        }
        Curso curso = buscar(cursoId);
        if (!curso.codigoConfere(codigoAcesso)) {
            throw new BusinessRuleException("Código inválido");
        }
        Aluno aluno = repositorioUsuario.buscarAlunoPorId(solicitante.id())
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado"));
        aluno.adicionarCurso(curso.id());
        repositorioUsuario.salvar(aluno);
        return curso;
    }

    private void validarMonitores(List<UUID> monitorIds) {
        if (monitorIds == null || monitorIds.isEmpty()) {
            throw new BusinessRuleException("Informe ao menos um monitor");
        }
        for (UUID monitorId : monitorIds) {
            repositorioUsuario.buscarMonitorPorId(monitorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Monitor não encontrado: " + monitorId));
        }
    }

    private static void exigirAdmin(Usuario solicitante) {
        if (solicitante.tipo() != TipoUsuario.ADMIN) {
            throw new ForbiddenException("Apenas o admin gerencia cursos");
        }
    }
}
