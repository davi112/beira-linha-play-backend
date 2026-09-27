package br.icei.beiralinhaplay.aplicacao;

import br.icei.beiralinhaplay.aplicacao.usuario.ContaService;
import br.icei.beiralinhaplay.dominio.autenticacao.PasswordHasher;
import br.icei.beiralinhaplay.dominio.usuario.Admin;
import br.icei.beiralinhaplay.dominio.usuario.Aluno;
import br.icei.beiralinhaplay.dominio.usuario.Monitor;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;
import br.icei.beiralinhaplay.dominio.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContaServiceTest {

    private static final UUID CURSO_A = UUID.fromString("00000000-0000-4000-8000-0000000000a1");
    private static final UUID CURSO_B = UUID.fromString("00000000-0000-4000-8000-0000000000b1");
    private static final UUID MONITOR_A = UUID.fromString("00000000-0000-4000-8000-0000000000c1");
    private static final UUID MONITOR_B = UUID.fromString("00000000-0000-4000-8000-0000000000c2");
    private static final UUID ALUNO_ID = UUID.fromString("00000000-0000-4000-8000-0000000000d1");
    private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-4000-8000-0000000000e1");

    private final FakeUsuarios usuarios = new FakeUsuarios();
    private ContaService servico;
    private Monitor monitorA;
    private Aluno aluno;
    private Admin admin;

    @BeforeEach
    void setup() {
        monitorA = new Monitor(MONITOR_A, "Ana", "ana@icei.br", "hash", List.of(CURSO_A), "ICEI");
        Monitor monitorB = new Monitor(MONITOR_B, "Bia", "bia@icei.br", "hash", List.of(CURSO_B), "ICEI");
        aluno = new Aluno(ALUNO_ID, "Gu", "gu@icei.br", "hash", List.of(CURSO_A), "Gu", 0, "");
        admin = new Admin(ADMIN_ID, "Admin", null, "hash");
        usuarios.monitores.add(monitorA);
        usuarios.monitores.add(monitorB);
        servico = new ContaService(usuarios, new CodificadorFake());
    }

    @Test
    void adminListaTodosOsMonitores() {
        assertEquals(2, servico.listarMonitores(admin).size());
    }

    @Test
    void monitorListaTodosOsMonitores() {
        assertEquals(2, servico.listarMonitores(monitorA).size());
    }

    @Test
    void alunoListaMonitores() {
        assertEquals(2, servico.listarMonitores(aluno).size());
    }

    static final class CodificadorFake implements PasswordHasher {
        @Override
        public String codificar(String senhaCrua) {
            return "hash-" + senhaCrua;
        }

        @Override
        public boolean confere(String senhaCrua, String senhaHash) {
            return senhaHash.equals(codificar(senhaCrua));
        }
    }

    static final class FakeUsuarios implements UsuarioRepository {
        final List<Monitor> monitores = new ArrayList<>();

        @Override
        public Usuario salvar(Usuario usuario) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Usuario> buscarPorId(UUID id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Usuario> buscarNaoExpiradoPorId(UUID id, java.time.LocalDate hoje) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Aluno> buscarAlunoPorId(UUID id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Monitor> buscarMonitorPorId(UUID id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Aluno> buscarAlunoPorApelido(String apelido) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Monitor> buscarMonitorPorEmail(String email) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Admin> buscarAdminPorNome(String nome) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existeApelido(String apelido, UUID ignorarId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existeEmail(String email, UUID ignorarId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existeNomeAdmin(String nome, UUID ignorarId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Monitor> listarMonitores() {
            return List.copyOf(monitores);
        }

        @Override
        public List<Aluno> listarAlunos() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Aluno> listarAlunosDoCurso(UUID cursoId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Usuario> buscarPorEmail(String email) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Usuario> listarPorEmail(String email) {
            throw new UnsupportedOperationException();
        }
    }
}
