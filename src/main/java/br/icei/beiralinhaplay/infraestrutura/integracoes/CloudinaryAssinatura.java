package br.icei.beiralinhaplay.infraestrutura.integracoes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.stream.Collectors;

final class CloudinaryAssinatura {

    private CloudinaryAssinatura() {
    }

    static String assinar(Map<String, String> parametros, String apiSecret) {
        String payload = parametros.entrySet().stream()
                .filter(entrada -> entrada.getValue() != null && !entrada.getValue().isBlank())
                .sorted(Map.Entry.comparingByKey())
                .map(entrada -> entrada.getKey() + "=" + entrada.getValue())
                .collect(Collectors.joining("&"));
        return sha1(payload + apiSecret);
    }

    private static String sha1(String valor) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-1")
                    .digest(valor.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
