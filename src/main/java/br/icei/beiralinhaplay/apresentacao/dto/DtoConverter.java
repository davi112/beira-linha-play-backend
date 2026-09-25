package br.icei.beiralinhaplay.apresentacao.dto;

import br.icei.beiralinhaplay.aplicacao.atividade.SalvarAtividadeCommand;
import br.icei.beiralinhaplay.aplicacao.autenticacao.AutenticarCommand;
import br.icei.beiralinhaplay.aplicacao.autenticacao.RegistrarCommand;
import br.icei.beiralinhaplay.aplicacao.curso.SalvarCursoCommand;
import br.icei.beiralinhaplay.aplicacao.questoes.GerarQuestoesCommand;
import br.icei.beiralinhaplay.aplicacao.questoes.QuestaoGerada;
import br.icei.beiralinhaplay.aplicacao.medalha.MedalhaComStatus;
import br.icei.beiralinhaplay.aplicacao.modulo.SalvarModuloCommand;
import br.icei.beiralinhaplay.aplicacao.ranking.RankingPosition;
import br.icei.beiralinhaplay.aplicacao.tentativa.EnviarTentativaCommand;
import br.icei.beiralinhaplay.aplicacao.tentativa.TentativaResult;
import br.icei.beiralinhaplay.aplicacao.usuario.AtualizarContaCommand;
import br.icei.beiralinhaplay.aplicacao.usuario.CriarAdminCommand;
import br.icei.beiralinhaplay.dominio.atividade.Atividade;
import br.icei.beiralinhaplay.dominio.curso.Curso;
import br.icei.beiralinhaplay.dominio.modulo.Modulo;
import br.icei.beiralinhaplay.dominio.resposta.Resposta;
import br.icei.beiralinhaplay.dominio.tentativa.Tentativa;
import br.icei.beiralinhaplay.dominio.usuario.Admin;
import br.icei.beiralinhaplay.dominio.usuario.Aluno;
import br.icei.beiralinhaplay.dominio.usuario.Monitor;
import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class DtoConverter {

    private DtoConverter() {
    }

    public static String id(UUID id) {
        return id == null ? null : String.valueOf(id);
    }

    public static UUID id(String id) {
        return UUID.fromString(id);
    }

    public static AutenticarCommand comando(LoginRequest req) {
        return new AutenticarCommand(req.tipo(), req.apelido(), req.email(), req.nome(), req.senha());
    }

    public static RegistrarCommand comando(CadastroRequest req) {
        return new RegistrarCommand(req.tipo(), req.nome(), req.apelido(), req.email(), req.senha(), req.cursoOrigem());
    }

    public static AtualizarContaCommand comando(AtualizarContaRequest req) {
        return new AtualizarContaCommand(req.nome(), req.apelido(), req.email(), req.senha());
    }

    public static CriarAdminCommand comando(CriarAdminRequest req) {
        return new CriarAdminCommand(req.nome(), req.senha());
    }

    public static SalvarCursoCommand comando(SalvarCursoRequest req) {
        return new SalvarCursoCommand(req.nome(), req.monitorIds().stream().map(DtoConverter::id).toList());
    }

    public static SalvarModuloCommand comando(SalvarModuloRequest req) {
        return new SalvarModuloCommand(req.nome());
    }

    public static GerarQuestoesCommand comando(GerarQuestoesRequest req) {
        return new GerarQuestoesCommand(req.mensagem(), req.quantidadeQuestoes());
    }

    public static GerarQuestoesResponse questoesGeradas(List<QuestaoGerada> questoes) {
        return new GerarQuestoesResponse(
                questoes.stream()
                        .map(q -> new GerarQuestoesResponse.QuestaoGeradaResponse(
                                q.enunciado(),
                                q.valor(),
                                q.alternativas().stream()
                                        .map(a -> new GerarQuestoesResponse.AlternativaGeradaResponse(
                                                a.descricao(),
                                                a.correta()
                                        ))
                                        .toList()
                        ))
                        .toList()
        );
    }

    public static SalvarAtividadeCommand comando(SalvarAtividadeRequest req) {
        return new SalvarAtividadeCommand(
                req.titulo(),
                req.questoes().stream()
                        .map(q -> new SalvarAtividadeCommand.QuestaoCommand(
                                q.enunciado(),
                                q.valor(),
                                q.alternativas().stream()
                                        .map(a -> new SalvarAtividadeCommand.AlternativaCommand(a.descricao(), a.correta()))
                                        .toList()
                        ))
                        .toList()
        );
    }

    public static EnviarTentativaCommand comando(EnviarTentativaRequest req) {
        Map<UUID, UUID> mapa = req.respostas().stream()
                .collect(Collectors.toMap(r -> id(r.questaoId()), r -> id(r.alternativaId())));
        return new EnviarTentativaCommand(mapa);
    }

    public static UsuarioResponse usuario(Usuario usuario) {
        return switch (usuario) {
            case Aluno aluno -> new UsuarioResponse(
                    id(aluno.id()), aluno.nome(), aluno.email(), TipoUsuario.ALUNO,
                    aluno.cursoIds().stream().map(DtoConverter::id).toList(),
                    aluno.apelido(), aluno.pontos(), aluno.imagemPerfil(), null
            );
            case Monitor monitor -> new UsuarioResponse(
                    id(monitor.id()), monitor.nome(), monitor.email(), TipoUsuario.MONITOR,
                    monitor.cursoIds().stream().map(DtoConverter::id).toList(),
                    null, null, null, monitor.cursoOrigem()
            );
            case Admin admin -> new UsuarioResponse(
                    id(admin.id()), admin.nome(), admin.email(), TipoUsuario.ADMIN,
                    List.of(), null, null, null, null
            );
            default -> throw new IllegalStateException();
        };
    }

    public static CursoResponse curso(Curso curso, List<Modulo> modulos, boolean incluirCodigo) {
        return curso(curso, modulos, incluirCodigo, Map.of(), Map.of());
    }

    public static CursoResponse curso(
            Curso curso,
            List<Modulo> modulos,
            boolean incluirCodigo,
            Map<UUID, String> nomesMonitores
    ) {
        return curso(curso, modulos, incluirCodigo, nomesMonitores, Map.of());
    }

    public static CursoResponse curso(
            Curso curso,
            List<Modulo> modulos,
            boolean incluirCodigo,
            Map<UUID, String> nomesMonitores,
            Map<UUID, List<Atividade>> atividadesPorModulo
    ) {
        List<String> nomes = curso.monitorIds().stream()
                .map(nomesMonitores::get)
                .filter(nome -> nome != null && !nome.isBlank())
                .toList();
        return new CursoResponse(
                id(curso.id()),
                curso.nome(),
                incluirCodigo ? curso.codigoAcesso() : null,
                curso.monitorIds().stream().map(DtoConverter::id).toList(),
                nomes,
                modulos.stream()
                        .map(m -> new CursoResponse.ModuloResumo(
                                id(m.id()),
                                m.nome(),
                                id(m.cursoId()),
                                atividadesPorModulo.getOrDefault(m.id(), List.of()).stream()
                                        .map(a -> new ModuloResponse.AtividadeResumo(
                                                id(a.id()),
                                                a.titulo(),
                                                a.quantQuestoes(),
                                                id(a.moduloId()),
                                                a.xpTotal()
                                        ))
                                        .toList()
                        ))
                        .toList()
        );
    }

    public static ModuloResponse modulo(Modulo modulo, List<Atividade> atividades) {
        return new ModuloResponse(
                id(modulo.id()),
                modulo.nome(),
                id(modulo.cursoId()),
                atividades.stream()
                        .map(a -> new ModuloResponse.AtividadeResumo(
                                id(a.id()),
                                a.titulo(),
                                a.quantQuestoes(),
                                id(a.moduloId()),
                                a.xpTotal()
                        ))
                        .toList()
        );
    }

    public static AtividadeResponse atividade(Atividade atividade, boolean incluirGabarito) {
        return new AtividadeResponse(
                id(atividade.id()),
                atividade.titulo(),
                atividade.quantQuestoes(),
                id(atividade.moduloId()),
                atividade.questoes().stream()
                        .map(q -> new AtividadeResponse.QuestaoResposta(
                                id(q.id()),
                                q.enunciado(),
                                q.valor(),
                                q.alternativas().stream()
                                        .map(a -> new AtividadeResponse.AlternativaResposta(
                                                id(a.id()),
                                                a.descricao(),
                                                incluirGabarito ? a.correta() : null
                                        ))
                                        .toList()
                        ))
                        .toList()
        );
    }

    public static TentativaResponse tentativa(Tentativa tentativa) {
        return new TentativaResponse(
                id(tentativa.id()),
                tentativa.dataEnvio().toString(),
                tentativa.pontuacaoObtida(),
                id(tentativa.alunoId()),
                id(tentativa.atividadeId()),
                tentativa.respostas().stream().map(DtoConverter::resposta).toList()
        );
    }

    public static ResultadoTentativaResponse resultado(TentativaResult resultado) {
        return new ResultadoTentativaResponse(
                tentativa(resultado.tentativa()),
                resultado.tentativasUsadas(),
                resultado.melhorPontuacao(),
                resultado.pontosDelta(),
                resultado.pontosTotais(),
                resultado.concluida()
        );
    }

    public static MedalhaResponse medalha(MedalhaComStatus item) {
        return new MedalhaResponse(
                id(item.medalha().id()),
                item.medalha().nome(),
                item.medalha().imagemUrl(),
                item.medalha().pontosMin(),
                item.conquistada()
        );
    }

    public static RankingResponse ranking(RankingPosition item) {
        return new RankingResponse(
                item.posicao(),
                id(item.id()),
                item.nome(),
                item.apelido(),
                item.pontos(),
                item.imagemPerfil()
        );
    }

    private static TentativaResponse.RespostaItem resposta(Resposta resposta) {
        return new TentativaResponse.RespostaItem(
                id(resposta.id()),
                resposta.correta(),
                id(resposta.questaoId()),
                id(resposta.alternativaId())
        );
    }
}
