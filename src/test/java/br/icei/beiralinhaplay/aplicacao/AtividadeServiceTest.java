package br.icei.beiralinhaplay.aplicacao;

import br.icei.beiralinhaplay.aplicacao.atividade.AtividadeService;
import br.icei.beiralinhaplay.dominio.alternativa.Alternativa;
import br.icei.beiralinhaplay.dominio.atividade.Atividade;
import br.icei.beiralinhaplay.dominio.atividade.AtividadeRepository;
import br.icei.beiralinhaplay.dominio.compartilhado.ForbiddenException;
import br.icei.beiralinhaplay.dominio.curso.Curso;
import br.icei.beiralinhaplay.dominio.curso.CursoRepository;
import br.icei.beiralinhaplay.dominio.modulo.Modulo;
import br.icei.beiralinhaplay.dominio.modulo.ModuloRepository;
import br.icei.beiralinhaplay.dominio.questao.Questao;
import br.icei.beiralinhaplay.dominio.usuario.Monitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AtividadeServiceTest {

    private static final UUID CURSO_ID = UUID.fromString("00000000-0000-4000-8000-000000000030");
    private static final UUID MODULO_ID = UUID.fromString("00000000-0000-4000-8000-000000000031");
    private static final UUID ATIVIDADE_ID = UUID.fromString("00000000-0000-4000-8000-000000000032");
    private static final UUID MONITOR_ID = UUID.fromString("00000000-0000-4000-8000-000000000033");

    private final FakeAtividades atividades = new FakeAtividades();
    private final FakeModulos modulos = new FakeModulos();
    private final FakeCursos cursos = new FakeCursos();
    private AtividadeService servico;
    private Monitor monitor;

    @BeforeEach
    void setup() {
        monitor = new Monitor(MONITOR_ID, "Ana", "ana@icei.br", "hash", List.of(CURSO_ID), "ICEI");
        cursos.porId.put(CURSO_ID, new Curso(CURSO_ID, "ED", "ABC123", List.of(MONITOR_ID), List.of(MODULO_ID)));
        modulos.porId.put(MODULO_ID, new Modulo(MODULO_ID, "Listas", CURSO_ID, List.of(ATIVIDADE_ID)));
        atividades.porId.put(ATIVIDADE_ID, new Atividade(
                ATIVIDADE_ID,
                "Quiz",
                MODULO_ID,
                List.of(new Questao(
                        null,
                        "Quanto vale 1+1?",
                        1,
                        List.of(
                                new Alternativa(null, "2", true),
                                new Alternativa(null, "3", false)
                        )
                ))
        ));
        servico = new AtividadeService(atividades, modulos, cursos);
    }

    @Test
    void bloqueiaLeituraQuandoMonitorNaoAlocado() {
        Monitor outro = new Monitor(
                UUID.fromString("00000000-0000-4000-8000-000000000034"),
                "Bia",
                "bia@icei.br",
                "hash",
                List.of(),
                "ICEI"
        );

        assertThrows(ForbiddenException.class, () -> servico.buscar(outro, ATIVIDADE_ID));
    }

    @Test
    void permiteLeituraQuandoMonitorAlocado() {
        assertEquals("Quiz", servico.buscar(monitor, ATIVIDADE_ID).titulo());
    }

    static final class FakeAtividades implements AtividadeRepository {
        final Map<UUID, Atividade> porId = new HashMap<>();

        @Override
        public Atividade salvar(Atividade atividade) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Atividade> buscarPorId(UUID id) {
            return Optional.ofNullable(porId.get(id));
        }

        @Override
        public List<Atividade> listarPorModulo(UUID moduloId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void excluir(UUID id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean possuiTentativas(UUID atividadeId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean possuiTentativasNoModulo(UUID moduloId) {
            throw new UnsupportedOperationException();
        }
    }

    static final class FakeModulos implements ModuloRepository {
        final Map<UUID, Modulo> porId = new HashMap<>();

        @Override
        public Modulo salvar(Modulo modulo) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Modulo> buscarPorId(UUID id) {
            return Optional.ofNullable(porId.get(id));
        }

        @Override
        public List<Modulo> listarPorCurso(UUID cursoId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void excluir(UUID id) {
            throw new UnsupportedOperationException();
        }
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
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Curso> listar() {
            throw new UnsupportedOperationException();
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
}
