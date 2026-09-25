package br.icei.beiralinhaplay.infraestrutura.integracoes;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CloudinaryUrlsTest {

    @Test
    void extraiPublicIdComPastaEVersao() {
        assertEquals(
                "medalhas/abc",
                CloudinaryUrls.publicId(
                        "https://res.cloudinary.com/demo/image/upload/v1234/medalhas/abc.png"
                )
        );
    }

    @Test
    void ignoraTransformacaoNaUrl() {
        assertEquals(
                "medalhas/abc",
                CloudinaryUrls.publicId(
                        "https://res.cloudinary.com/demo/image/upload/c_fill,h_200,w_200/v1/medalhas/abc.png"
                )
        );
    }

    @Test
    void rejeitaUrlInvalida() {
        assertNull(CloudinaryUrls.publicId("https://example.com/foto.png"));
    }

    @Test
    void reconheceContaPeloHost() {
        assertTrue(CloudinaryUrls.daConta(
                "https://res.cloudinary.com/demo/image/upload/v1/medalhas/abc.png",
                "demo"
        ));
    }
}

class CloudinaryAssinaturaTest {

    @Test
    void assinaParametrosOrdenados() {
        String hash = CloudinaryAssinatura.assinar(
                Map.of("timestamp", "123", "folder", "medalhas"),
                "secret"
        );
        assertEquals(40, hash.length());
        assertEquals(
                CloudinaryAssinatura.assinar(Map.of("folder", "medalhas", "timestamp", "123"), "secret"),
                hash
        );
    }
}
