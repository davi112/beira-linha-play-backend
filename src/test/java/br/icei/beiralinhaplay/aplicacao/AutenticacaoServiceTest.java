package br.icei.beiralinhaplay.aplicacao;

import br.icei.beiralinhaplay.aplicacao.autenticacao.AutenticarCommand;
import br.icei.beiralinhaplay.aplicacao.autenticacao.RegistrarCommand;
import br.icei.beiralinhaplay.aplicacao.autenticacao.AutenticacaoResult;
import br.icei.beiralinhaplay.aplicacao.autenticacao.AutenticacaoService;
import br.icei.beiralinhaplay.dominio.autenticacao.PasswordHasher;
import br.icei.beiralinhaplay.dominio.autenticacao.RefreshTokenGenerator;
import br.icei.beiralinhaplay.dominio.autenticacao.AccessTokenProvider;
import br.icei.beiralinhaplay.dominio.autenticacao.TokenAtualizacaoRepository;
import br.icei.beiralinhaplay.dominio.autenticacao.TokenAtualizacao;
import br.icei.beiralinhaplay.dominio.compartilhado.InvalidCredentialsException;
import br.icei.beiralinhaplay.dominio.usuario.Aluno;
import br.icei.beiralinhaplay.dominio.usuario.UsuarioRepository;
import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AutenticacaoServiceTest {

    private static final UUID GUSTAVO_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");

    private final FakeUsuario usuarios = new FakeUsuario();
    private final FakeTokens tokens = new FakeTokens();
    private AutenticacaoService servico;

    @BeforeEach
    void setup() {
        usuarios.salvar(new Aluno(GUSTAVO_ID, "Gustavo", null, "hash-123456", List.of(), "Gu", 40, ""));
        servico = new AutenticacaoService(
                usuarios,
                tokens,
                new CodificadorFake(),
                new AccessTokenProvider() {
                    @Override
                    public String gerar(Usuario usuario) {
                        return "jwt-" + usuario.id();
                    }

                    @Override
                    public ClaimsToken validar(String token) {
                        return new ClaimsToken(GUSTAVO_ID, "ALUNO");
                    }
                },
                new GeradorFake(),
                Duration.ofDays(7),
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void loginAlunoComApelido() {
        AutenticacaoResult resultado = servico.autenticar(
                new AutenticarCommand(TipoUsuario.ALUNO, "Gu", null, null, "123456")
        );
        assertEquals("Gu", ((Aluno) resultado.usuario()).apelido());
        assertEquals("jwt-" + GUSTAVO_ID, resultado.tokenAcesso());
        assertNotNull(resultado.tokenAtualizacao());
        assertEquals(1, tokens.porHash.size());
    }

    @Test
    void loginAlunoComEmail() {
        usuarios.salvar(new Aluno(
                UUID.randomUUID(),
                "Maria Silva",
                "maria@email.com",
                "hash-123456",
                List.of(),
                "maria",
                0,
                ""
        ));
        AutenticacaoResult resultado = servico.autenticar(
                new AutenticarCommand(TipoUsuario.ALUNO, null, "maria@email.com", null, "123456")
        );
        assertEquals("maria@email.com", resultado.usuario().email());
    }

    @Test
    void emailRepetidoExigeApelido() {
        usuarios.salvar(new Aluno(
                UUID.randomUUID(),
                "Maria Silva",
                "maria@email.com",
                "hash-123456",
                List.of(),
                "maria",
                0,
                ""
        ));
        usuarios.salvar(new Aluno(
                UUID.randomUUID(),
                "João Silva",
                "maria@email.com",
                "hash-123456",
                List.of(),
                "joao",
                0,
                ""
        ));

        InvalidCredentialsException erro = assertThrows(InvalidCredentialsException.class, () -> servico.autenticar(
                new AutenticarCommand(TipoUsuario.ALUNO, null, "maria@email.com", null, "123456")
        ));
        assertEquals("Este e-mail está em mais de uma conta. Entre com o apelido.", erro.getMessage());

        AutenticacaoResult resultado = servico.autenticar(
                new AutenticarCommand(TipoUsuario.ALUNO, "joao", null, null, "123456")
        );
        assertEquals("joao", ((Aluno) resultado.usuario()).apelido());
    }

    @Test
    void loginRecusaAcessoExpirado() {
        Aluno maria = new Aluno(
                UUID.randomUUID(),
                "Maria Silva",
                "maria@email.com",
                "hash-123456",
                List.of(),
                "maria",
                0,
                ""
        );
        maria.definirAcessoExpiraEm(LocalDate.of(2025, 12, 31));
        usuarios.salvar(maria);

        InvalidCredentialsException erro = assertThrows(InvalidCredentialsException.class, () -> servico.autenticar(
                new AutenticarCommand(TipoUsuario.ALUNO, "maria", null, null, "123456")
        ));
        assertEquals("O acesso deste usuário expirou.", erro.getMessage());
    }

    @Test
    void loginRejeitaSenhaErrada() {
        assertThrows(InvalidCredentialsException.class, () -> servico.autenticar(
                new AutenticarCommand(TipoUsuario.ALUNO, "Gu", null, null, "errada")
        ));
    }

    @Test
    void cadastroAlunoEmiteSessao() {
        AutenticacaoResult resultado = servico.registrar(
                new RegistrarCommand(TipoUsuario.ALUNO, "Nova", "Novinha", null, "123456", null)
        );
        assertEquals(TipoUsuario.ALUNO, resultado.usuario().tipo());
        assertEquals("Novinha", ((Aluno) resultado.usuario()).apelido());
    }

    static final class CodificadorFake implements PasswordHasher {
        @Override
        public String codificar(String senhaCrua) {
            return "hash-" + senhaCrua;
        }

        @Override
        public boolean confere(String senhaCrua, String senhaHash) {
            return senhaHash.equals("hash-" + senhaCrua);
        }
    }

    static final class GeradorFake implements RefreshTokenGenerator {
        private int seq;

        @Override
        public String gerarTokenOpaco() {
            return "opaco-" + (++seq);
        }

        @Override
        public String hash(String tokenOpaco) {
            return "h-" + tokenOpaco;
        }
    }

    static final class FakeTokens implements TokenAtualizacaoRepository {
        final Map<String, TokenAtualizacao> porHash = new HashMap<>();

        @Override
        public TokenAtualizacao salvar(TokenAtualizacao token) {
            if (token.id() == null) {
                token.definirId(UUID.randomUUID());
            }
            porHash.put(token.tokenHash(), token);
            return token;
        }

        @Override
        public Optional<TokenAtualizacao> buscarPorHash(String tokenHash) {
            return Optional.ofNullable(porHash.get(tokenHash));
        }

        @Override
        public void revogarTodosDoUsuario(UUID usuarioId) {
            porHash.values().stream()
                    .filter(t -> t.usuarioId().equals(usuarioId))
                    .forEach(TokenAtualizacao::revogar);
        }
    }

    static final class FakeUsuario implements UsuarioRepository {
        final Map<UUID, Usuario> porId = new HashMap<>();

        @Override
        public Usuario salvar(Usuario usuario) {
            if (usuario.id() == null) {
                usuario.definirId(UUID.randomUUID());
            }
            porId.put(usuario.id(), usuario);
            return usuario;
        }

        @Override
        public Optional<Usuario> buscarPorId(UUID id) {
            return Optional.ofNullable(porId.get(id));
        }

        @Override
        public Optional<Usuario> buscarNaoExpiradoPorId(UUID id, java.time.LocalDate hoje) {
            return buscarPorId(id).filter(usuario -> !usuario.acessoExpirado(hoje));
        }

        @Override
        public Optional<Aluno> buscarAlunoPorId(UUID id) {
            return buscarPorId(id).filter(Aluno.class::isInstance).map(Aluno.class::cast);
        }

        @Override
        public Optional<br.icei.beiralinhaplay.dominio.usuario.Monitor> buscarMonitorPorId(UUID id) {
            return Optional.empty();
        }

        @Override
        public Optional<Aluno> buscarAlunoPorApelido(String apelido) {
            return porId.values().stream()
                    .filter(Aluno.class::isInstance)
                    .map(Aluno.class::cast)
                    .filter(a -> a.apelido().equalsIgnoreCase(apelido))
                    .findFirst();
        }

        @Override
        public Optional<br.icei.beiralinhaplay.dominio.usuario.Monitor> buscarMonitorPorEmail(String email) {
            return Optional.empty();
        }

        @Override
        public Optional<br.icei.beiralinhaplay.dominio.usuario.Admin> buscarAdminPorNome(String nome) {
            return Optional.empty();
        }

        @Override
        public boolean existeApelido(String apelido, UUID ignorarId) {
            return buscarAlunoPorApelido(apelido)
                    .filter(a -> ignorarId == null || !a.id().equals(ignorarId))
                    .isPresent();
        }

        @Override
        public boolean existeEmail(String email, UUID ignorarId) {
            return false;
        }

        @Override
        public boolean existeNomeAdmin(String nome, UUID ignorarId) {
            return false;
        }

        @Override
        public List<br.icei.beiralinhaplay.dominio.usuario.Monitor> listarMonitores() {
            return List.of();
        }

        @Override
        public List<Aluno> listarAlunos() {
            return List.of();
        }

        @Override
        public List<Aluno> listarAlunosDoCurso(UUID cursoId) {
            return List.of();
        }

        @Override
        public Optional<Usuario> buscarPorEmail(String email) {
            List<Usuario> encontrados = listarPorEmail(email);
            if (encontrados.size() != 1) {
                return Optional.empty();
            }
            return Optional.of(encontrados.getFirst());
        }

        @Override
        public List<Usuario> listarPorEmail(String email) {
            if (email == null || email.isBlank()) {
                return List.of();
            }
            return porId.values().stream()
                    .filter(usuario -> email.equalsIgnoreCase(usuario.email()))
                    .toList();
        }
    }
}
