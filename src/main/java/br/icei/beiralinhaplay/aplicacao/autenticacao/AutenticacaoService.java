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
import br.icei.beiralinhaplay.dominio.usuario.Aluno;
import br.icei.beiralinhaplay.dominio.usuario.Monitor;
import br.icei.beiralinhaplay.dominio.usuario.UsuarioRepository;
import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
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
        exigirAcesso(usuario);
        return criarToken(usuario);
    }

    public AutenticacaoResult registrar(RegistrarCommand comando) {
        validarSenha(comando.senha());
        Usuario criado = switch (comando.tipo()) {
            case ALUNO -> registrarAluno(comando);
            case MONITOR -> registrarMonitor(comando);
            case ADMIN -> throw new BusinessRuleException("Admin não pode se cadastrar por esta rota");
        };
        return criarToken(criado);
    }

    public AutenticacaoResult renovar(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidCredentialsException("Sessão expirada");
        }
        Instant agora = Instant.now(relogio);
        String hash = geradorTokenAtualizacao.hash(token);
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
        exigirAcesso(usuario);
        return criarToken(usuario);
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

    private AutenticacaoResult criarToken(Usuario usuario) {
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
            case ALUNO -> localizarAluno(comando);
            case MONITOR -> repositorioUsuario.buscarMonitorPorEmail(comando.email())
                    .orElseThrow(() -> new InvalidCredentialsException("E-mail ou senha inválidos"));
            case ADMIN -> repositorioUsuario.buscarAdminPorNome(comando.nome())
                    .orElseThrow(() -> new InvalidCredentialsException("Nome ou senha inválidos"));
        };
    }

    private Usuario localizarAluno(AutenticarCommand comando) {
        String email = comando.email();
        String apelido = comando.apelido();
        if ((email == null || email.isBlank()) && apelido != null && apelido.contains("@")) {
            email = apelido;
            apelido = null;
        }
        if (email != null && !email.isBlank()) {
            List<Usuario> comEmail = repositorioUsuario.listarPorEmail(email).stream()
                    .filter(Aluno.class::isInstance)
                    .toList();
            if (comEmail.size() > 1) {
                throw new InvalidCredentialsException("Este e-mail está em mais de uma conta. Entre com o apelido.");
            }
            if (comEmail.size() == 1) {
                return comEmail.getFirst();
            }
            throw new InvalidCredentialsException("Apelido, e-mail ou senha inválidos");
        }
        if (apelido == null || apelido.isBlank()) {
            throw new InvalidCredentialsException("Apelido, e-mail ou senha inválidos");
        }
        return repositorioUsuario.buscarAlunoPorApelido(apelido)
                .orElseThrow(() -> new InvalidCredentialsException("Apelido, e-mail ou senha inválidos"));
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

    private void exigirAcesso(Usuario usuario) {
        if (usuario.acessoExpirado(LocalDate.now(relogio))) {
            throw new InvalidCredentialsException("O acesso deste usuário expirou.");
        }
    }

    private static void validarSenha(String senha) {
        if (senha == null || senha.length() < DomainRules.SENHA_MINIMA) {
            throw new BusinessRuleException("A senha deve ter pelo menos 6 caracteres");
        }
    }

    private static String mensagemCredencial(TipoUsuario tipo) {
        return switch (tipo) {
            case ALUNO -> "Apelido, e-mail ou senha inválidos";
            case MONITOR -> "E-mail ou senha inválidos";
            case ADMIN -> "Nome ou senha inválidos";
        };
    }
}
