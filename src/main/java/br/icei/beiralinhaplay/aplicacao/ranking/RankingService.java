package br.icei.beiralinhaplay.aplicacao.ranking;

import br.icei.beiralinhaplay.dominio.compartilhado.ResourceNotFoundException;
import br.icei.beiralinhaplay.dominio.curso.CursoRepository;
import br.icei.beiralinhaplay.dominio.usuario.Aluno;
import br.icei.beiralinhaplay.dominio.usuario.UsuarioRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class RankingService {

    private final UsuarioRepository repositorioUsuario;
    private final CursoRepository repositorioCurso;

    public RankingService(UsuarioRepository repositorioUsuario, CursoRepository repositorioCurso) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioCurso = repositorioCurso;
    }

    public List<RankingPosition> listar(UUID cursoId) {
        List<Aluno> alunos = cursoId == null
                ? repositorioUsuario.listarAlunos()
                : alunosDoCurso(cursoId);

        List<Aluno> ordenados = alunos.stream()
                .sorted(Comparator.comparingInt(Aluno::pontos).reversed().thenComparing(Aluno::apelido))
                .toList();
        AtomicInteger posicao = new AtomicInteger(1);
        return ordenados.stream()
                .map(aluno -> RankingPosition.de(posicao.getAndIncrement(), aluno))
                .toList();
    }

    private List<Aluno> alunosDoCurso(UUID cursoId) {
        repositorioCurso.buscarPorId(cursoId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado"));
        return repositorioUsuario.listarAlunosDoCurso(cursoId);
    }
}
