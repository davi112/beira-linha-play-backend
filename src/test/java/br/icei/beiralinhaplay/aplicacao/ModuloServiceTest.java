package br.icei.beiralinhaplay.aplicacao;

import br.icei.beiralinhaplay.aplicacao.modulo.ModuloService;
import br.icei.beiralinhaplay.dominio.atividade.Atividade;
import br.icei.beiralinhaplay.dominio.atividade.AtividadeRepository;
import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;
import br.icei.beiralinhaplay.dominio.curso.Curso;
import br.icei.beiralinhaplay.dominio.curso.CursoRepository;
import br.icei.beiralinhaplay.dominio.modulo.Modulo;
import br.icei.beiralinhaplay.dominio.modulo.ModuloRepository;
import br.icei.beiralinhaplay.dominio.usuario.Monitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuloServiceTest {

    private static final UUID CURSO_ID = UUID.fromString("00000000-0000-4000-8000-000000000020");
    private static final UUID MODULO_ID = UUID.fromString("00000000-0000-4000-8000-000000000021");
    private static final UUID MONITOR_ID = UUID.fromString("00000000-0000-4000-8000-000000000022");

    private final FakeModulos modulos = new FakeModulos();
    private final FakeCursos cursos = new FakeCursos();
    private final FakeAtividades atividades = new FakeAtividades();
    private ModuloService servico;
    private Monitor monitor;

    @BeforeEach
    void setup() {
        monitor = new Monitor(MONITOR_ID, "Ana", "ana@icei.br", "hash", List.of(CURSO_ID), "ICEI");
        cursos.porId.put(CURSO_ID, new Curso(CURSO_ID, "ED", "ABC123", List.of(MONITOR_ID), List.of(MODULO_ID)));
        modulos.porId.put(MODULO_ID, new Modulo(MODULO_ID, "Listas", CURSO_ID, List.of()));
        servico = new ModuloService(modulos, cursos, atividades);
    }

    @Test
    void excluiQuandoNaoHaTentativas() {
        servico.excluir(monitor, MODULO_ID);

        assertTrue(modulos.excluidos.contains(MODULO_ID));
        assertFalse(modulos.porId.containsKey(MODULO_ID));
    }

    @Test
    void bloqueiaExclusaoQuandoHaTentativas() {
        atividades.modulosComTentativas.add(MODULO_ID);

        BusinessRuleException erro = assertThrows(
                BusinessRuleException.class,
                () -> servico.excluir(monitor, MODULO_ID)
        );

        assertEquals(
                "Não é possível excluir um módulo que possui atividades com tentativas",
                erro.getMessage()
        );
        assertFalse(modulos.excluidos.contains(MODULO_ID));
        assertTrue(modulos.porId.containsKey(MODULO_ID));
    }

    static final class FakeModulos implements ModuloRepository {
        final Map<UUID, Modulo> porId = new HashMap<>();
        final Set<UUID> excluidos = new HashSet<>();

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
            excluidos.add(id);
            porId.remove(id);
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

    static final class FakeAtividades implements AtividadeRepository {
        final Set<UUID> modulosComTentativas = new HashSet<>();

        @Override
        public Atividade salvar(Atividade atividade) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Atividade> buscarPorId(UUID id) {
            throw new UnsupportedOperationException();
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
            return modulosComTentativas.contains(moduloId);
        }
    }
}
