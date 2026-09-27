package br.icei.beiralinhaplay.infraestrutura.integracoes;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

final class CloudinaryUrls {

    private static final Pattern TRANSFORMACAO = Pattern.compile("^[a-z]+_");

    private CloudinaryUrls() {
    }

    static String publicId(String imagemUrl) {
        if (imagemUrl == null || imagemUrl.isBlank()) {
            return null;
        }
        URI uri;
        try {
            uri = URI.create(imagemUrl);
        } catch (IllegalArgumentException ex) {
            return null;
        }
        String path = uri.getPath();
        if (path == null) {
            return null;
        }
        int marker = path.indexOf("/upload/");
        if (marker < 0) {
            return null;
        }
        List<String> partes = new ArrayList<>();
        for (String parte : path.substring(marker + "/upload/".length()).split("/")) {
            if (parte.isBlank() || parte.contains(",") || TRANSFORMACAO.matcher(parte).find()) {
                continue;
            }
            partes.add(parte);
        }
        if (!partes.isEmpty() && partes.getFirst().matches("v\\d+")) {
            partes.removeFirst();
        }
        if (partes.isEmpty()) {
            return null;
        }
        String ultimo = partes.getLast();
        int ponto = ultimo.lastIndexOf('.');
        if (ponto > 0) {
            partes.set(partes.size() - 1, ultimo.substring(0, ponto));
        }
        return String.join("/", partes);
    }

    static boolean daConta(String imagemUrl, String cloudName) {
        if (imagemUrl == null || cloudName == null || cloudName.isBlank()) {
            return false;
        }
        String host = "res.cloudinary.com/" + cloudName.toLowerCase(Locale.ROOT);
        return imagemUrl.toLowerCase(Locale.ROOT).contains(host);
    }
}
