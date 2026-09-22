package br.icei.beiralinhaplay.aplicacao.atividade;

import br.icei.beiralinhaplay.aplicacao.curso.AcessoCurso;
import br.icei.beiralinhaplay.dominio.alternativa.Alternativa;
import br.icei.beiralinhaplay.dominio.atividade.Atividade;
import br.icei.beiralinhaplay.dominio.atividade.AtividadeRepository;
import br.icei.beiralinhaplay.dominio.compartilhado.ForbiddenException;
import br.icei.beiralinhaplay.dominio.compartilhado.ResourceNotFoundException;
import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;
import br.icei.beiralinhaplay.dominio.curso.Curso;
import br.icei.beiralinhaplay.dominio.curso.CursoRepository;
import br.icei.beiralinhaplay.dominio.modulo.Modulo;
import br.icei.beiralinhaplay.dominio.modulo.ModuloRepository;
import br.icei.beiralinhaplay.dominio.questao.Questao;
import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;

import java.util.List;
import java.util.UUID;

public class AtividadeService {

    private final AtividadeRepository repositorioAtividade;
    private final ModuloRepository repositorioModulo;
    private final CursoRepository repositorioCurso;

    public AtividadeService(
            AtividadeRepository repositorioAtividade,
            ModuloRepository repositorioModulo,
            CursoRepository repositorioCurso
    ) {
        this.repositorioAtividade = repositorioAtividade;
        this.repositorioModulo = repositorioModulo;
        this.repositorioCurso = repositorioCurso;
    }

    public List<Atividade> listarPorModulo(UUID moduloId) {
        return repositorioAtividade.listarPorModulo(moduloId);
    }

    public Atividade buscar(UUID id) {
        return repositorioAtividade.buscarPorId(id).orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada"));
    }

    public Atividade buscar(Usuario solicitante, UUID id) {
        Atividade atividade = buscar(id);
        AcessoCurso.exigirLeitura(solicitante, cursoDaAtividade(atividade));
        return atividade;
    }

    public Atividade criar(Usuario solicitante, UUID moduloId, SalvarAtividadeCommand comando) {
        Modulo modulo = modulo(moduloId);
        exigirMonitorDoCurso(solicitante, modulo.cursoId());
        Atividade atividade = montar(null, moduloId, comando);
        return repositorioAtividade.salvar(atividade);
    }

    public Atividade atualizar(Usuario solicitante, UUID id, SalvarAtividadeCommand comando) {
        Atividade atual = buscar(id);
        exigirMonitorDoCurso(solicitante, modulo(atual.moduloId()).cursoId());
        if (repositorioAtividade.possuiTentativas(id)) {
            throw new BusinessRuleException("Não é possível editar uma atividade que já possui tentativas");
        }
        Atividade atualizada = montar(id, atual.moduloId(), comando);
        return repositorioAtividade.salvar(atualizada);
    }

    public void excluir(Usuario solicitante, UUID id) {
        Atividade atual = buscar(id);
        exigirMonitorDoCurso(solicitante, modulo(atual.moduloId()).cursoId());
        if (repositorioAtividade.possuiTentativas(id)) {
            throw new BusinessRuleException("Não é possível excluir uma atividade que já possui tentativas");
        }
        repositorioAtividade.excluir(id);
    }

    private Atividade montar(UUID id, UUID moduloId, SalvarAtividadeCommand comando) {
        List<Questao> questoes = comando.questoes().stream()
                .map(q -> new Questao(
                        null,
                        q.enunciado(),
                        q.valor(),
                        q.alternativas().stream().map(a ->
                                new Alternativa(null, a.descricao(), a.correta())
                        ).toList()
                ))
                .toList();

        return new Atividade(id, comando.titulo(), moduloId, questoes);
    }

    private Modulo modulo(UUID moduloId) {
        return repositorioModulo.buscarPorId(moduloId) .orElseThrow(() -> new ResourceNotFoundException("Módulo não encontrado"));
    }

    private Curso cursoDaAtividade(Atividade atividade) {
        return repositorioCurso.buscarPorId(modulo(atividade.moduloId()).cursoId()).orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado"));
    }

    private void exigirMonitorDoCurso(Usuario solicitante, UUID cursoId) {
        Curso curso = repositorioCurso.buscarPorId(cursoId).orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado"));

        if (solicitante.tipo() != TipoUsuario.MONITOR || !curso.monitorIds().contains(solicitante.id())) {
            throw new ForbiddenException("Apenas o monitor do curso pode alterar atividades");
        }
    }
}
