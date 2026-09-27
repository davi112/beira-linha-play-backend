package br.icei.beiralinhaplay.aplicacao.usuario;

import br.icei.beiralinhaplay.dominio.autenticacao.PasswordHasher;
import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;
import br.icei.beiralinhaplay.dominio.compartilhado.ForbiddenException;
import br.icei.beiralinhaplay.dominio.curso.Curso;
import br.icei.beiralinhaplay.dominio.curso.CursoRepository;
import br.icei.beiralinhaplay.dominio.compartilhado.ResourceNotFoundException;
import br.icei.beiralinhaplay.dominio.importacao.LogImportacao;
import br.icei.beiralinhaplay.dominio.importacao.LogImportacaoRepository;
import br.icei.beiralinhaplay.dominio.usuario.Aluno;
import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;
import br.icei.beiralinhaplay.dominio.usuario.UsuarioRepository;
import br.icei.beiralinhaplay.infraestrutura.integracoes.sympla.CodificadorEventoSympla;
import br.icei.beiralinhaplay.infraestrutura.integracoes.sympla.ImportadorParticipantesSympla;
import br.icei.beiralinhaplay.infraestrutura.integracoes.sympla.ImportadorParticipantesSympla.Evento;
import br.icei.beiralinhaplay.infraestrutura.integracoes.sympla.ImportadorParticipantesSympla.Inscrito;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ImportarInscritosService {

    private static final int NOME_CURSO_MAX = 120;
    private static final int APELIDO_MAX = 40;
    private static final DateTimeFormatter DATA_EXIBICAO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ImportadorParticipantesSympla importadorParticipantes;
    private final CodificadorEventoSympla codificadorEvento;
    private final UsuarioRepository repositorioUsuario;
    private final CursoRepository repositorioCurso;
    private final LogImportacaoRepository repositorioLog;
    private final PasswordHasher codificadorSenha;

    public ImportarInscritosService(
            ImportadorParticipantesSympla importadorParticipantes,
            CodificadorEventoSympla codificadorEvento,
            UsuarioRepository repositorioUsuario,
            CursoRepository repositorioCurso,
            LogImportacaoRepository repositorioLog,
            PasswordHasher codificadorSenha
    ) {
        this.importadorParticipantes = importadorParticipantes;
        this.codificadorEvento = codificadorEvento;
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioCurso = repositorioCurso;
        this.repositorioLog = repositorioLog;
        this.codificadorSenha = codificadorSenha;
    }

    public List<EventoDisponivel> listarEventos(Usuario solicitante, int ano) {
        exigirAdmin(solicitante);
        if (ano < 2000 || ano > 2100) {
            throw new BusinessRuleException("Informe um ano válido");
        }
        return importadorParticipantes.listarEventos(ano).stream()
                .map(evento -> new EventoDisponivel(
                        codificadorEvento.codificar(evento.id()),
                        evento.nome(),
                        formatarData(evento.inicio()),
                        formatarData(evento.fim())
                ))
                .toList();
    }

    public LogImportacaoConsulta importarInscritos(Usuario solicitante, String referencia) {
        exigirAdmin(solicitante);
        String idEvento = codificadorEvento.decodificar(referencia);
        Evento evento = importadorParticipantes.buscarEvento(idEvento);

        Map<String, Curso> cursosPorNome = new HashMap<>();
        for (Curso curso : repositorioCurso.listar()) {
            cursosPorNome.putIfAbsent(chaveCurso(curso.nome()), curso);
        }

        Set<UUID> cursosNovos = new HashSet<>();
        int alunosNovos = 0;
        LocalDate acessoExpiraEm = LocalDate.now().plusMonths(3);
        Map<UUID, Aluno> importados = new LinkedHashMap<>();
        for (Inscrito inscrito : importadorParticipantes.importar(idEvento)) {
            Curso curso = garantirCurso(inscrito.nomeCurso(), cursosPorNome, cursosNovos);
            List<Usuario> comEmail = repositorioUsuario.listarPorEmail(inscrito.email());
            if (comEmail.stream().anyMatch(usuario -> !(usuario instanceof Aluno))) {
                throw new BusinessRuleException("E-mail já usado por outro tipo de usuário: " + inscrito.email());
            }
            Aluno existente = comEmail.stream()
                    .filter(Aluno.class::isInstance)
                    .map(Aluno.class::cast)
                    .filter(aluno -> mesmoNome(aluno.nome(), inscrito.nome()))
                    .findFirst()
                    .orElse(null);
            if (existente == null) {
                String senha = senhaPadrao(inscrito.nome());
                Aluno novo = new Aluno(
                        null,
                        inscrito.nome(),
                        inscrito.email(),
                        codificadorSenha.codificar(senha),
                        curso == null ? List.of() : List.of(curso.id()),
                        apelidoLivre(inscrito.apelido()),
                        0,
                        ""
                );
                novo.concluirDefinicaoSenha(false);
                novo.definirAcessoExpiraEm(acessoExpiraEm);
                existente = (Aluno) repositorioUsuario.salvar(novo);
                alunosNovos++;
            } else {
                if (curso != null) {
                    existente.adicionarCurso(curso.id());
                }
                existente.definirAcessoExpiraEm(acessoExpiraEm);
                existente = (Aluno) repositorioUsuario.salvar(existente);
            }
            importados.put(existente.id(), existente);
        }

        LogImportacao salvo = repositorioLog.salvar(new LogImportacao(
                null,
                evento.nome().isBlank() ? "Evento sem nome" : evento.nome(),
                evento.url(),
                alunosNovos,
                cursosNovos.size(),
                Instant.now(),
                solicitante.id()
        ));
        for (Aluno aluno : importados.values()) {
            aluno.definirLogImportacao(salvo.id());
            repositorioUsuario.salvar(aluno);
        }
        return consulta(salvo, solicitante.nome());
    }

    public List<LogImportacaoConsulta> listarLogs(Usuario solicitante) {
        exigirAdmin(solicitante);
        return repositorioLog.listar().stream()
                .map(log -> consulta(log, nomeAdmin(log.adminId())))
                .toList();
    }

    public List<Aluno> listarAlunosDoLog(Usuario solicitante, UUID logId) {
        exigirAdmin(solicitante);
        if (!repositorioLog.existe(logId)) {
            throw new ResourceNotFoundException("Importação não encontrada");
        }
        return repositorioLog.listarAlunos(logId);
    }

    private Curso garantirCurso(String nomeCurso, Map<String, Curso> cursosPorNome, Set<UUID> cursosNovos) {
        if (nomeCurso == null || nomeCurso.isBlank()) {
            return null;
        }
        String nome = nomeCurso.trim();
        if (nome.length() > NOME_CURSO_MAX) {
            nome = nome.substring(0, NOME_CURSO_MAX).trim();
        }
        String chave = chaveCurso(nome);
        Curso existente = cursosPorNome.get(chave);
        if (existente != null) {
            return existente;
        }
        Curso curso = new Curso(null, nome, null, List.of(), List.of());
        int tentativas = 0;
        while (repositorioCurso.existeCodigoAcesso(curso.codigoAcesso(), null) && tentativas < 8) {
            curso.regenerarCodigoAcesso();
            tentativas++;
        }
        if (repositorioCurso.existeCodigoAcesso(curso.codigoAcesso(), null)) {
            throw new BusinessRuleException("Não foi possível gerar um código de acesso único");
        }
        Curso salvo = repositorioCurso.salvar(curso);
        cursosPorNome.put(chave, salvo);
        cursosNovos.add(salvo.id());
        return salvo;
    }

    private String apelidoLivre(String sugerido) {
        String base = sugerido == null || sugerido.isBlank() ? "aluno" : sugerido.trim();
        if (base.length() > APELIDO_MAX) {
            base = base.substring(0, APELIDO_MAX);
        }
        if (!repositorioUsuario.existeApelido(base, null)) {
            return base;
        }
        for (int indice = 2; indice < 1000; indice++) {
            String sufixo = "-" + indice;
            int limite = APELIDO_MAX - sufixo.length();
            String candidato = (base.length() > limite ? base.substring(0, limite) : base) + sufixo;
            if (!repositorioUsuario.existeApelido(candidato, null)) {
                return candidato;
            }
        }
        throw new BusinessRuleException("Não foi possível gerar um apelido único");
    }

    private String nomeAdmin(UUID adminId) {
        return repositorioUsuario.buscarPorId(adminId).map(Usuario::nome).orElse("Administrador");
    }

    private static LogImportacaoConsulta consulta(LogImportacao log, String adminNome) {
        return new LogImportacaoConsulta(
                log.id(),
                log.nomeEvento(),
                log.urlEvento(),
                log.quantidadeAlunos(),
                log.quantidadeCursos(),
                log.dataImportacao(),
                adminNome
        );
    }

    private static void exigirAdmin(Usuario solicitante) {
        if (solicitante == null || solicitante.tipo() != TipoUsuario.ADMIN) {
            throw new ForbiddenException("Somente usuários com permissão podem realizar essa operação");
        }
    }

    private static boolean mesmoNome(String atual, String recebido) {
        return chaveNome(atual).equals(chaveNome(recebido));
    }

    private static String senhaPadrao(String nome) {
        String primeiro = nome == null ? "" : nome.trim();
        int espaco = primeiro.indexOf(' ');
        if (espaco > 0) {
            primeiro = primeiro.substring(0, espaco);
        }
        primeiro = primeiro.toLowerCase(Locale.ROOT);
        if (primeiro.isBlank()) {
            primeiro = "aluno";
        }
        return primeiro + Year.now().getValue();
    }

    private static String chaveCurso(String nome) {
        return chaveNome(nome);
    }

    private static String formatarData(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String data = valor.trim();
        if (data.length() >= 10) {
            data = data.substring(0, 10);
        }
        try {
            return LocalDate.parse(data).format(DATA_EXIBICAO);
        } catch (DateTimeParseException ex) {
            return valor.trim();
        }
    }

    private static String chaveNome(String nome) {
        if (nome == null) {
            return "";
        }
        return nome.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    public record EventoDisponivel(String referencia, String nome, String inicio, String fim) {
    }

    public record LogImportacaoConsulta(
            UUID id,
            String nomeEvento,
            String urlEvento,
            int quantidadeAlunos,
            int quantidadeCursos,
            Instant dataImportacao,
            String adminNome
    ) {
    }
}
