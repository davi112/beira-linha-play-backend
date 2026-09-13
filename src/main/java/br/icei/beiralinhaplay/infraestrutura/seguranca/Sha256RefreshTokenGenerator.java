package br.icei.beiralinhaplay.infraestrutura.seguranca;

import br.icei.beiralinhaplay.dominio.autenticacao.RefreshTokenGenerator;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

@Component
public class Sha256RefreshTokenGenerator implements RefreshTokenGenerator {

    private final SecureRandom aleatorio = new SecureRandom();

    @Override
    public String gerarTokenOpaco() {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    @Override
    public String hash(String tokenOpaco) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(tokenOpaco.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
