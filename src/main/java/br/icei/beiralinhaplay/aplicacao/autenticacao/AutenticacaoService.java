package br.icei.beiralinhaplay.aplicacao.autenticacao;

import br.icei.beiralinhaplay.dominio.autenticacao.PasswordHasher;
import br.icei.beiralinhaplay.dominio.autenticacao.RefreshTokenGenerator;
import br.icei.beiralinhaplay.dominio.autenticacao.AccessTokenProvider;
import br.icei.beiralinhaplay.dominio.autenticacao.TokenAtualizacaoRepository;
import br.icei.beiralinhaplay.dominio.autenticacao.TokenAtualizacao;
import br.icei.beiralinhaplay.dominio.compartilhado.InvalidCredentialsException;
import br.icei.beiralinhaplay.dominio.compartilhado.ResourceNotFoundException;
import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;
import br.icei.beiralinhaplay.dominio.compartilhado.DomainRules;
import br.icei.beiralinhaplay.dominio.usuario.Admin;
import br.icei.beiralinhaplay.dominio.usuario.Aluno;
import br.icei.beiralinhaplay.dominio.usuario.Monitor;
import br.icei.beiralinhaplay.dominio.usuario.UsuarioRepository;
import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AutenticacaoService {

    private final UsuarioRepository repositorioUsuario;
    private final TokenAtualizacaoRepository repositorioTokenAtualizacao;
    private final PasswordHasher codificadorSenha;
    private final AccessTokenProvider provedorTokenAcesso;
    private final RefreshTokenGenerator geradorTokenAtualizacao;
    private final Duration validadeRefresh;
    private final Clock relogio;

    public AutenticacaoService(
            UsuarioRepository repositorioUsuario,
            TokenAtualizacaoRepository repositorioTokenAtualizacao,
            PasswordHasher codificadorSenha,
            AccessTokenProvider provedorTokenAcesso,
            RefreshTokenGenerator geradorTokenAtualizacao,
            Duration validadeRefresh,
            Clock relogio
    ) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioTokenAtualizacao = repositorioTokenAtualizacao;
        this.codificadorSenha = codificadorSenha;
        this.provedorTokenAcesso = provedorTokenAcesso;
        this.geradorTokenAtualizacao = geradorTokenAtualizacao;
        this.validadeRefresh = validadeRefresh;
        this.relogio = relogio;
    }

    public AutenticacaoResult autenticar(AutenticarCommand comando) {
        Usuario usuario = localizarParaLogin(comando);
        if (!codificadorSenha.confere(comando.senha(), usuario.senhaHash())) {
            throw new InvalidCredentialsException(mensagemCredencial(comando.tipo()));
        }
        return emitirSessao(usuario);
    }

    public AutenticacaoResult registrar(RegistrarCommand comando) {
        validarSenha(comando.senha());
        Usuario criado = switch (comando.tipo()) {
            case ALUNO -> registrarAluno(comando);
            case MONITOR -> registrarMonitor(comando);
            case ADMIN -> throw new BusinessRuleException("Admin não pode se cadastrar por esta rota");
        };
        return emitirSessao(criado);
    }

    public AutenticacaoResult renovar(String tokenOpaco) {
        if (tokenOpaco == null || tokenOpaco.isBlank()) {
            throw new InvalidCredentialsException("Sessão expirada");
        }
        Instant agora = Instant.now(relogio);
        String hash = geradorTokenAtualizacao.hash(tokenOpaco);
        TokenAtualizacao atual = repositorioTokenAtualizacao.buscarPorHash(hash)
                .orElseThrow(() -> new InvalidCredentialsException("Sessão expirada"));

        if (!atual.valido(agora)) {
            repositorioTokenAtualizacao.revogarTodosDoUsuario(atual.usuarioId());
            throw new InvalidCredentialsException("Sessão expirada");
        }

        atual.revogar();
        repositorioTokenAtualizacao.salvar(atual);

        Usuario usuario = repositorioUsuario.buscarPorId(atual.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return emitirSessao(usuario);
    }

    public void encerrar(String tokenOpaco) {
        if (tokenOpaco == null || tokenOpaco.isBlank()) {
            return;
        }
        geradorTokenAtualizacao.hash(tokenOpaco);
        repositorioTokenAtualizacao.buscarPorHash(geradorTokenAtualizacao.hash(tokenOpaco))
                .ifPresent(token -> {
                    token.revogar();
                    repositorioTokenAtualizacao.salvar(token);
                });
    }

    public Usuario usuarioAutenticado(UUID usuarioId) {
        return repositorioUsuario.buscarPorId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    private AutenticacaoResult emitirSessao(Usuario usuario) {
        Instant agora = Instant.now(relogio);
        String opaco = geradorTokenAtualizacao.gerarTokenOpaco();
        TokenAtualizacao persistido = new TokenAtualizacao(
                null,
                usuario.id(),
                geradorTokenAtualizacao.hash(opaco),
                agora.plus(validadeRefresh),
                false
        );
        repositorioTokenAtualizacao.salvar(persistido);
        return new AutenticacaoResult(usuario, provedorTokenAcesso.gerar(usuario), opaco);
    }

    private Usuario localizarParaLogin(AutenticarCommand comando) {
        return switch (comando.tipo()) {
            case ALUNO -> repositorioUsuario.buscarAlunoPorApelido(comando.apelido())
                    .orElseThrow(() -> new InvalidCredentialsException("Apelido ou senha inválidos"));
            case MONITOR -> repositorioUsuario.buscarMonitorPorEmail(comando.email())
                    .orElseThrow(() -> new InvalidCredentialsException("E-mail ou senha inválidos"));
            case ADMIN -> repositorioUsuario.buscarAdminPorNome(comando.nome())
                    .orElseThrow(() -> new InvalidCredentialsException("Nome ou senha inválidos"));
        };
    }

    private Aluno registrarAluno(RegistrarCommand comando) {
        if (comando.apelido() == null || comando.apelido().isBlank()) {
            throw new BusinessRuleException("Informe o apelido");
        }
        if (repositorioUsuario.existeApelido(comando.apelido().trim(), null)) {
            throw new BusinessRuleException("Apelido já em uso");
        }
        Aluno aluno = new Aluno(
                null,
                comando.nome(),
                comando.email(),
                codificadorSenha.codificar(comando.senha()),
                List.of(),
                comando.apelido(),
                0,
                ""
        );
        return (Aluno) repositorioUsuario.salvar(aluno);
    }

    private Monitor registrarMonitor(RegistrarCommand comando) {
        if (comando.email() == null || comando.email().isBlank()) {
            throw new BusinessRuleException("Informe um e-mail válido");
        }
        if (repositorioUsuario.existeEmail(comando.email(), null)) {
            throw new BusinessRuleException("E-mail já em uso");
        }
        Monitor monitor = new Monitor(
                null,
                comando.nome(),
                comando.email(),
                codificadorSenha.codificar(comando.senha()),
                List.of(),
                comando.cursoOrigem()
        );
        return (Monitor) repositorioUsuario.salvar(monitor);
    }

    private static void validarSenha(String senha) {
        if (senha == null || senha.length() < DomainRules.SENHA_MINIMA) {
            throw new BusinessRuleException("A senha deve ter pelo menos 6 caracteres");
        }
    }

    private static String mensagemCredencial(TipoUsuario tipo) {
        return switch (tipo) {
            case ALUNO -> "Apelido ou senha inválidos";
            case MONITOR -> "E-mail ou senha inválidos";
            case ADMIN -> "Nome ou senha inválidos";
        };
    }
}
