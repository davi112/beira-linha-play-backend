package br.icei.beiralinhaplay.aplicacao;

import br.icei.beiralinhaplay.aplicacao.curso.CursoService;
import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;
import br.icei.beiralinhaplay.dominio.compartilhado.ForbiddenException;
import br.icei.beiralinhaplay.dominio.curso.Curso;
import br.icei.beiralinhaplay.dominio.curso.CursoRepository;
import br.icei.beiralinhaplay.dominio.usuario.Admin;
import br.icei.beiralinhaplay.dominio.usuario.Aluno;
import br.icei.beiralinhaplay.dominio.usuario.Monitor;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;
import br.icei.beiralinhaplay.dominio.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CursoServiceTest {

    private static final UUID CURSO_A = UUID.fromString("00000000-0000-4000-8000-0000000000a1");
    private static final UUID CURSO_B = UUID.fromString("00000000-0000-4000-8000-0000000000b1");
    private static final UUID MONITOR_ID = UUID.fromString("00000000-0000-4000-8000-0000000000c1");
    private static final UUID ALUNO_ID = UUID.fromString("00000000-0000-4000-8000-0000000000d1");
    private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-4000-8000-0000000000e1");

    private final FakeCursos cursos = new FakeCursos();
    private final FakeUsuarios usuarios = new FakeUsuarios();
    private CursoService servico;
    private Monitor monitor;
    private Aluno aluno;
    private Admin admin;

    @BeforeEach
    void setup() {
        monitor = new Monitor(MONITOR_ID, "Ana", "ana@icei.br", "hash", List.of(CURSO_A), "ICEI");
        aluno = new Aluno(ALUNO_ID, "Gu", "gu@icei.br", "hash", List.of(CURSO_A), "Gu", 0, "");
        admin = new Admin(ADMIN_ID, "Admin", null, "hash");
        usuarios.alunos.put(ALUNO_ID, aluno);
        cursos.porId.put(CURSO_A, new Curso(CURSO_A, "Cálculo", "CALC-AAAA", List.of(MONITOR_ID), List.of()));
        cursos.porId.put(CURSO_B, new Curso(CURSO_B, "Programação", "PROG-BBBB", List.of(), List.of()));
        servico = new CursoService(cursos, usuarios);
    }

    @Test
    void adminListaTodosOsCursos() {
        assertEquals(2, servico.listar(admin).size());
    }

    @Test
    void monitorListaSomenteCursosQueMinistra() {
        List<Curso> lista = servico.listar(monitor);

        assertEquals(1, lista.size());
        assertEquals(CURSO_A, lista.getFirst().id());
    }

    @Test
    void alunoListaSomenteCursosInscritos() {
        List<Curso> lista = servico.listar(aluno);

        assertEquals(1, lista.size());
        assertEquals(CURSO_A, lista.getFirst().id());
    }

    @Test
    void monitorNaoLeCursoAlheio() {
        assertThrows(ForbiddenException.class, () -> servico.buscar(monitor, CURSO_B));
    }

    @Test
    void alunoNaoLeCursoEmQueNaoEstaInscrito() {
        assertThrows(ForbiddenException.class, () -> servico.buscar(aluno, CURSO_B));
    }

    @Test
    void inscreveAlunoPeloCodigo() {
        Aluno novo = new Aluno(
                UUID.fromString("00000000-0000-4000-8000-0000000000d2"),
                "Lia",
                "lia@icei.br",
                "hash",
                List.of(),
                "Lia",
                0,
                ""
        );
        usuarios.alunos.put(novo.id(), novo);

        Curso curso = servico.inscreverPorCodigo(novo, "calc-aaaa");

        assertEquals(CURSO_A, curso.id());
        assertTrue(novo.inscritoOuMinistra(CURSO_A));
    }

    @Test
    void codigoInexistenteNaoVazaCurso() {
        BusinessRuleException erro = assertThrows(
                BusinessRuleException.class,
                () -> servico.inscreverPorCodigo(aluno, "NAO-EXISTE")
        );

        assertEquals("Código inválido", erro.getMessage());
    }

    @Test
    void monitorNaoEntraComCodigo() {
        assertThrows(ForbiddenException.class, () -> servico.inscreverPorCodigo(monitor, "CALC-AAAA"));
    }

    static final class FakeCursos implements CursoRepository {
        final Map<UUID, Curso> porId = new HashMap<>();

        @Override
        public Curso salvar(Curso curso) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Curso> buscarPorId(UUID id) {
            return Optional.ofNullable(porId.get(id));
        }

        @Override
        public Optional<Curso> buscarPorCodigoAcesso(String codigo) {
            if (codigo == null || codigo.isBlank()) {
                return Optional.empty();
            }
            String normalizado = codigo.trim().toUpperCase(Locale.ROOT);
            return porId.values().stream()
                    .filter(curso -> curso.codigoAcesso().equalsIgnoreCase(normalizado))
                    .findFirst();
        }

        @Override
        public List<Curso> listar() {
            return new ArrayList<>(porId.values());
        }

        @Override
        public void excluir(UUID id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existeCodigoAcesso(String codigo, UUID ignorarId) {
            throw new UnsupportedOperationException();
        }
    }

    static final class FakeUsuarios implements UsuarioRepository {
        final Map<UUID, Aluno> alunos = new HashMap<>();

        @Override
        public Usuario salvar(Usuario usuario) {
            if (usuario instanceof Aluno aluno) {
                alunos.put(aluno.id(), aluno);
            }
            return usuario;
        }

        @Override
        public Optional<Usuario> buscarPorId(UUID id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Aluno> buscarAlunoPorId(UUID id) {
            return Optional.ofNullable(alunos.get(id));
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
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Aluno> listarAlunos() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Aluno> listarAlunosDoCurso(UUID cursoId) {
            throw new UnsupportedOperationException();
        }
    }
}
