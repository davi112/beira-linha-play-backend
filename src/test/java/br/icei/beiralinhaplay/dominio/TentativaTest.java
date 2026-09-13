package br.icei.beiralinhaplay.dominio;

import br.icei.beiralinhaplay.dominio.alternativa.Alternativa;
import br.icei.beiralinhaplay.dominio.atividade.Atividade;
import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;
import br.icei.beiralinhaplay.dominio.questao.Questao;
import br.icei.beiralinhaplay.dominio.tentativa.Tentativa;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TentativaTest {

    private final Atividade atividade = atividade();

    @Test
    void corrigePontuacaoSomandoValorDasAcertos() {
        Tentativa tentativa = Tentativa.corrigir(
                id(1),
                atividade,
                Map.of(id(10), id(101), id(11), id(111)),
                Instant.parse("2026-01-01T12:00:00Z"),
                0,
                0
        );

        assertEquals(5, tentativa.pontuacaoObtida());
        assertEquals(2, tentativa.respostas().size());
        assertTrue(tentativa.respostas().get(0).correta());
        assertTrue(tentativa.respostas().get(1).correta());
        assertEquals(5, tentativa.pontosDelta(0));
        assertEquals(0, tentativa.pontosDelta(5));
        assertEquals(2, tentativa.pontosDelta(3));
    }

    @Test
    void bloqueiaTerceiraTentativa() {
        assertThrows(BusinessRuleException.class, () -> Tentativa.corrigir(
                id(1),
                atividade,
                Map.of(id(10), id(101), id(11), id(111)),
                Instant.now(),
                2,
                0
        ));
    }

    @Test
    void bloqueiaQuandoJaGabaritou() {
        assertThrows(BusinessRuleException.class, () -> Tentativa.corrigir(
                id(1),
                atividade,
                Map.of(id(10), id(101), id(11), id(111)),
                Instant.now(),
                1,
                5
        ));
    }

    @Test
    void atividadeConcluidaComDuasTentativasOuGabarito() {
        assertFalse(atividade.concluida(1, 3));
        assertTrue(atividade.concluida(2, 3));
        assertTrue(atividade.concluida(1, 5));
    }

    @Test
    void questaoExigeUmaAlternativaCorreta() {
        assertThrows(BusinessRuleException.class, () -> new Questao(
                id(1),
                "Pergunta",
                1,
                List.of(
                        new Alternativa(id(1), "A", false),
                        new Alternativa(id(2), "B", false)
                )
        ));
    }

    private static Atividade atividade() {
        Questao q1 = new Questao(id(10), "Q1", 2, List.of(
                new Alternativa(id(100), "errada", false),
                new Alternativa(id(101), "certa", true)
        ));
        Questao q2 = new Questao(id(11), "Q2", 3, List.of(
                new Alternativa(id(110), "errada", false),
                new Alternativa(id(111), "certa", true),
                new Alternativa(id(112), "outra", false)
        ));
        return new Atividade(id(50), "Quiz", id(7), List.of(q1, q2));
    }

    private static UUID id(int n) {
        return UUID.fromString(String.format("00000000-0000-4000-8000-%012d", n));
    }
}
