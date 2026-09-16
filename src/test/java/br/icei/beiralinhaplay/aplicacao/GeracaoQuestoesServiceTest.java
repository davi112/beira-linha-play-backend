package br.icei.beiralinhaplay.aplicacao;

import br.icei.beiralinhaplay.aplicacao.ia.GeracaoQuestoesService;
import br.icei.beiralinhaplay.aplicacao.ia.GeradorQuestoes;
import br.icei.beiralinhaplay.aplicacao.ia.GerarQuestoesCommand;
import br.icei.beiralinhaplay.aplicacao.ia.QuestaoGerada;
import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;
import br.icei.beiralinhaplay.dominio.compartilhado.ForbiddenException;
import br.icei.beiralinhaplay.dominio.curso.Curso;
import br.icei.beiralinhaplay.dominio.curso.CursoRepository;
import br.icei.beiralinhaplay.dominio.modulo.Modulo;
import br.icei.beiralinhaplay.dominio.modulo.ModuloRepository;
import br.icei.beiralinhaplay.dominio.usuario.Aluno;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeracaoQuestoesServiceTest {

    private static final UUID CURSO_ID = UUID.fromString("00000000-0000-4000-8000-000000000010");
    private static final UUID MODULO_ID = UUID.fromString("00000000-0000-4000-8000-000000000011");
    private static final UUID MONITOR_ID = UUID.fromString("00000000-0000-4000-8000-000000000012");

    private static final String JSON_VALIDO = """
            {
              "questoes": [
                {
                  "enunciado": "O que é uma lista?",
                  "valor": 1,
                  "alternativas": [
                    { "descricao": "Uma coleção", "correta": true },
                    { "descricao": "Um número", "correta": false }
                  ]
                }
              ]
            }
            """;

    private final FakeModulos modulos = new FakeModulos();
    private final FakeCursos cursos = new FakeCursos();
    private final FakeGerador gerador = new FakeGerador();
    private GeracaoQuestoesService servico;
    private Monitor monitor;

    @BeforeEach
    void setup() {
        monitor = new Monitor(MONITOR_ID, "Ana", "ana@icei.br", "hash", List.of(CURSO_ID), "ICEI");
        cursos.porId.put(CURSO_ID, new Curso(CURSO_ID, "ED", "ABC123", List.of(MONITOR_ID), List.of(MODULO_ID)));
        modulos.porId.put(MODULO_ID, new Modulo(MODULO_ID, "Listas", CURSO_ID, List.of()));
        gerador.resposta = JSON_VALIDO;
        servico = new GeracaoQuestoesService(modulos, cursos, gerador);
    }

    @Test
    void monitorGeraQuestoes() {
        List<QuestaoGerada> questoes = servico.gerar(
                monitor,
                MODULO_ID,
                new GerarQuestoesCommand("crie perguntas sobre listas", null)
        );

        assertEquals(1, questoes.size());
        assertEquals("O que é uma lista?", questoes.getFirst().enunciado());
        assertEquals("crie perguntas sobre listas", gerador.ultimoPrompt);
        assertTrue(gerador.ultimaInstrucao.contains("enunciado"));
    }

    @Test
    void quantidadeEntraNoPrompt() {
        servico.gerar(monitor, MODULO_ID, new GerarQuestoesCommand("crie perguntas", 3));

        assertTrue(gerador.ultimoPrompt.contains("3 pergunta(s)"));
        assertTrue(gerador.ultimoPrompt.contains("gere exatamente 3"));
    }

    @Test
    void alunoNaoPodeGerar() {
        Aluno aluno = new Aluno(
                UUID.randomUUID(),
                "Gustavo",
                null,
                "hash",
                List.of(CURSO_ID),
                "Gu",
                0,
                ""
        );

        assertThrows(ForbiddenException.class, () -> servico.gerar(
                aluno,
                MODULO_ID,
                new GerarQuestoesCommand("crie perguntas", 2)
        ));
    }

    @Test
    void jsonInvalidoViraRegraDeNegocio() {
        gerador.resposta = "isso não é json";

        assertThrows(BusinessRuleException.class, () -> servico.gerar(
                monitor,
                MODULO_ID,
                new GerarQuestoesCommand("crie perguntas", null)
        ));
    }

    static final class FakeGerador implements GeradorQuestoes {
        String resposta;
        String ultimoPrompt;
        String ultimaInstrucao;

        @Override
        public String gerar(String prompt, String systemInstruction) {
            ultimoPrompt = prompt;
            ultimaInstrucao = systemInstruction;
            return resposta;
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
