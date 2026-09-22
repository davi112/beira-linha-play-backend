package br.icei.beiralinhaplay.aplicacao.tentativa;

import br.icei.beiralinhaplay.dominio.tentativa.Tentativa;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MonitoramentoReportTest {

    private static final UUID ALUNO = UUID.fromString("00000000-0000-4000-8000-000000000001");
    private static final UUID ATIVIDADE = UUID.fromString("00000000-0000-4000-8000-000000000002");

    @Test
    void escolheMaiorPontuacaoMesmoQueNaoSejaAUltima() {
        Tentativa melhor = tentativa(id(3), 8, "2026-01-01T12:00:00Z");
        Tentativa ultima = tentativa(id(4), 3, "2026-01-02T12:00:00Z");

        List<Tentativa> escolhidas = MonitoramentoReport.melhorPorAluno(List.of(ultima, melhor));

        assertEquals(1, escolhidas.size());
        assertEquals(id(3), escolhidas.getFirst().id());
        assertEquals(8, escolhidas.getFirst().pontuacaoObtida());
    }

    @Test
    void empateDePontosFicaComAMaisRecente() {
        Tentativa antiga = tentativa(id(5), 5, "2026-01-01T12:00:00Z");
        Tentativa recente = tentativa(id(6), 5, "2026-01-02T12:00:00Z");

        List<Tentativa> escolhidas = MonitoramentoReport.melhorPorAluno(List.of(antiga, recente));

        assertEquals(id(6), escolhidas.getFirst().id());
    }

    private static Tentativa tentativa(UUID id, int pontos, String iso) {
        return new Tentativa(id, Instant.parse(iso), pontos, ALUNO, ATIVIDADE, List.of());
    }

    private static UUID id(int n) {
        return UUID.fromString(String.format("00000000-0000-4000-8000-%012d", n));
    }
}
