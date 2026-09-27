package br.icei.beiralinhaplay.infraestrutura.integracoes.sympla;

import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;
import br.icei.beiralinhaplay.dominio.compartilhado.ServiceUnavailableException;
import br.icei.beiralinhaplay.infraestrutura.configuracao.ApplicationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class CodificadorEventoSympla {

    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final ApplicationProperties propriedades;

    public CodificadorEventoSympla(ApplicationProperties propriedades) {
        this.propriedades = propriedades;
    }

    public String codificar(String id) {
        if (id == null || id.isBlank()) {
            throw new BusinessRuleException("Evento inválido");
        }
        try {
            byte[] iv = new byte[IV_BYTES];
            ALEATORIO.nextBytes(iv);
            Cipher cifra = Cipher.getInstance("AES/GCM/NoPadding");
            cifra.init(Cipher.ENCRYPT_MODE, chave(), new GCMParameterSpec(TAG_BITS, iv));
            byte[] cifrado = cifra.doFinal(id.trim().getBytes(StandardCharsets.UTF_8));
            byte[] pacote = new byte[iv.length + cifrado.length];
            System.arraycopy(iv, 0, pacote, 0, iv.length);
            System.arraycopy(cifrado, 0, pacote, iv.length, cifrado.length);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(pacote);
        } catch (BusinessRuleException | ServiceUnavailableException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceUnavailableException("Importação do Sympla indisponível");
        }
    }

    public String decodificar(String referencia) {
        if (referencia == null || referencia.isBlank()) {
            throw new BusinessRuleException("Selecione o evento");
        }
        try {
            byte[] pacote = Base64.getUrlDecoder().decode(referencia.trim());
            if (pacote.length <= IV_BYTES) {
                throw new BusinessRuleException("Evento inválido");
            }
            byte[] iv = new byte[IV_BYTES];
            byte[] cifrado = new byte[pacote.length - IV_BYTES];
            System.arraycopy(pacote, 0, iv, 0, IV_BYTES);
            System.arraycopy(pacote, IV_BYTES, cifrado, 0, cifrado.length);
            Cipher cifra = Cipher.getInstance("AES/GCM/NoPadding");
            cifra.init(Cipher.DECRYPT_MODE, chave(), new GCMParameterSpec(TAG_BITS, iv));
            String id = new String(cifra.doFinal(cifrado), StandardCharsets.UTF_8).trim();
            if (id.isBlank()) {
                throw new BusinessRuleException("Evento inválido");
            }
            return id;
        } catch (BusinessRuleException | ServiceUnavailableException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessRuleException("Evento inválido");
        }
    }

    private SecretKeySpec chave() throws Exception {
        String segredo = propriedades.getJwt().getSecret();
        if (segredo == null || segredo.isBlank()) {
            throw new ServiceUnavailableException("Importação do Sympla indisponível");
        }
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(segredo.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(hash, "AES");
    }
}
