package br.icei.beiralinhaplay.aplicacao.tentativa;

import br.icei.beiralinhaplay.dominio.atividade.Atividade;
import br.icei.beiralinhaplay.dominio.atividade.AtividadeRepository;
import br.icei.beiralinhaplay.dominio.compartilhado.ForbiddenException;
import br.icei.beiralinhaplay.dominio.compartilhado.ResourceNotFoundException;
import br.icei.beiralinhaplay.dominio.curso.Curso;
import br.icei.beiralinhaplay.dominio.curso.CursoRepository;
import br.icei.beiralinhaplay.dominio.modulo.Modulo;
import br.icei.beiralinhaplay.dominio.modulo.ModuloRepository;
import br.icei.beiralinhaplay.dominio.tentativa.TentativaRepository;
import br.icei.beiralinhaplay.dominio.tentativa.Tentativa;
import br.icei.beiralinhaplay.dominio.usuario.Aluno;
import br.icei.beiralinhaplay.dominio.usuario.UsuarioRepository;
import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class TentativaService {

    private final TentativaRepository repositorioTentativa;
    private final AtividadeRepository repositorioAtividade;
    private final ModuloRepository repositorioModulo;
    private final CursoRepository repositorioCurso;
    private final UsuarioRepository repositorioUsuario;
    private final Clock relogio;

    public TentativaService(
            TentativaRepository repositorioTentativa,
            AtividadeRepository repositorioAtividade,
            ModuloRepository repositorioModulo,
            CursoRepository repositorioCurso,
            UsuarioRepository repositorioUsuario,
            Clock relogio
    ) {
        this.repositorioTentativa = repositorioTentativa;
        this.repositorioAtividade = repositorioAtividade;
        this.repositorioModulo = repositorioModulo;
        this.repositorioCurso = repositorioCurso;
        this.repositorioUsuario = repositorioUsuario;
        this.relogio = relogio;
    }

    public List<Tentativa> listarDoAluno(UUID alunoId, UUID atividadeId) {
        if (atividadeId != null) {
            return repositorioTentativa.listarPorAlunoEAtividade(alunoId, atividadeId);
        }
        return repositorioTentativa.listarPorAluno(alunoId);
    }

    public TentativaResult enviar(Usuario solicitante, UUID atividadeId, EnviarTentativaCommand comando) {
        if (solicitante.tipo() != TipoUsuario.ALUNO) {
            throw new ForbiddenException("Apenas alunos enviam tentativas");
        }
        Aluno aluno = repositorioUsuario.buscarAlunoPorId(solicitante.id())
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado"));
        Atividade atividade = repositorioAtividade.buscarPorId(atividadeId)
                .orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada"));

        garantirInscricao(aluno, atividade);

        List<Tentativa> anteriores = repositorioTentativa.listarPorAlunoEAtividade(aluno.id(), atividadeId);
        int usadas = anteriores.size();
        int melhorAnterior = anteriores.stream()
                .mapToInt(Tentativa::pontuacaoObtida)
                .max()
                .orElse(0);

        Tentativa nova = Tentativa.corrigir(
                aluno.id(),
                atividade,
                comando.alternativaPorQuestao(),
                Instant.now(relogio),
                usadas,
                melhorAnterior
        );
        Tentativa salva = repositorioTentativa.salvar(nova);

        int delta = salva.pontosDelta(melhorAnterior);
        if (delta > 0) {
            aluno.adicionarPontos(delta);
            repositorioUsuario.salvar(aluno);
        }

        return TentativaResult.de(atividade, salva, usadas + 1, melhorAnterior, aluno.pontos());
    }

    public MonitoramentoReport monitorar(Usuario solicitante, UUID atividadeId) {
        Atividade atividade = repositorioAtividade.buscarPorId(atividadeId)
                .orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada"));
        Modulo modulo = repositorioModulo.buscarPorId(atividade.moduloId())
                .orElseThrow(() -> new ResourceNotFoundException("Módulo não encontrado"));
        Curso curso = repositorioCurso.buscarPorId(modulo.cursoId())
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado"));

        if (solicitante.tipo() == TipoUsuario.ALUNO) {
            throw new ForbiddenException("Aluno não acessa o monitoramento");
        }
        if (solicitante.tipo() == TipoUsuario.MONITOR && !curso.monitorIds().contains(solicitante.id())) {
            throw new ForbiddenException("Apenas o monitor do curso acessa o monitoramento");
        }

        List<Aluno> turma = repositorioUsuario.listarAlunosDoCurso(curso.id());
        List<Tentativa> tentativas = repositorioTentativa.listarPorAtividade(atividadeId);
        List<Tentativa> ultimaPorAluno = tentativas.stream()
                .collect(java.util.stream.Collectors.groupingBy(Tentativa::alunoId))
                .values()
                .stream()
                .map(lista -> lista.stream().max(Comparator.comparing(Tentativa::dataEnvio)).orElseThrow())
                .toList();

        return MonitoramentoReport.calcular(atividade, turma, ultimaPorAluno);
    }

    private void garantirInscricao(Aluno aluno, Atividade atividade) {
        Modulo modulo = repositorioModulo.buscarPorId(atividade.moduloId())
                .orElseThrow(() -> new ResourceNotFoundException("Módulo não encontrado"));
        if (!aluno.inscritoOuMinistra(modulo.cursoId())) {
            throw new ForbiddenException("Inscreva-se no curso antes de jogar");
        }
    }
}
