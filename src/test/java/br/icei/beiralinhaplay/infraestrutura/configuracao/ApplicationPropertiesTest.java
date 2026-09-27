package br.icei.beiralinhaplay.infraestrutura.configuracao;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationPropertiesTest {

    @Test
    void semOrigensNaoAssumeLocalhost() {
        assertTrue(new ApplicationProperties().origensCors().isEmpty());
    }

    @Test
    void aceitaVariasOrigensNaMesmaLista() {
        ApplicationProperties propriedades = new ApplicationProperties();
        propriedades.setAllowedHosts(List.of(
                "http://localhost:5173",
                "http://192.168.0.10:5173"
        ));
        assertEquals(
                List.of("http://localhost:5173", "http://192.168.0.10:5173"),
                propriedades.origensCors()
        );
    }

    @Test
    void separaOrigensColadasNumaUnicaString() {
        ApplicationProperties propriedades = new ApplicationProperties();
        propriedades.setAllowedHosts(List.of(
                "http://localhost:5173, http://127.0.0.1:5173;http://192.168.0.10:5173"
        ));
        assertEquals(
                List.of(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://192.168.0.10:5173"
                ),
                propriedades.origensCors()
        );
    }
}
