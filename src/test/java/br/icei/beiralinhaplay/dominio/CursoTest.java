package br.icei.beiralinhaplay.dominio;

import br.icei.beiralinhaplay.dominio.curso.Curso;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CursoTest {

    @Test
    void codigoAcessoEhCaseInsensitive() {
        Curso curso = new Curso(
                UUID.fromString("00000000-0000-4000-8000-000000000001"),
                "Cálculo 1",
                "CALC-2026-A",
                List.of(UUID.fromString("00000000-0000-4000-8000-000000000002")),
                List.of()
        );
        assertTrue(curso.codigoConfere("calc-2026-a"));
        assertFalse(curso.codigoConfere("errado"));
    }
}
