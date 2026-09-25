package br.icei.beiralinhaplay.infraestrutura.integracoes;

import br.icei.beiralinhaplay.aplicacao.medalha.ArmazenamentoImagem;
import br.icei.beiralinhaplay.dominio.compartilhado.BadGatewayException;
import br.icei.beiralinhaplay.dominio.compartilhado.ServiceUnavailableException;
import br.icei.beiralinhaplay.infraestrutura.configuracao.ApplicationProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CloudinaryArmazenamentoImagem implements ArmazenamentoImagem {

    private static final String FALHA_UPLOAD = "Não foi possível enviar a imagem. Tente de novo.";
    private static final String FALHA_EXCLUSAO = "Não foi possível excluir a imagem no Cloudinary.";

    private final ApplicationProperties propriedades;
    private final RestClient restClient;

    public CloudinaryArmazenamentoImagem(ApplicationProperties propriedades) {
        this.propriedades = propriedades;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(30));
        this.restClient = RestClient.builder()
                .baseUrl("https://api.cloudinary.com/v1_1")
                .requestFactory(factory)
                .build();
    }

    @Override
    public String enviar(byte[] conteudo, String contentType, String nomeArquivo) {
        ApplicationProperties.Cloudinary cloudinary = propriedades.getCloudinary();
        exigirConfigurado(cloudinary);
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        Map<String, String> assinar = new LinkedHashMap<>();
        assinar.put("timestamp", timestamp);
        if (!pasta(cloudinary).isBlank()) {
            assinar.put("folder", pasta(cloudinary));
        }
        String signature = CloudinaryAssinatura.assinar(assinar, cloudinary.getApiSecret());

        String arquivo = nomeArquivo == null || nomeArquivo.isBlank() ? "medalha" : nomeArquivo;
        ByteArrayResource resource = new ByteArrayResource(conteudo) {
            @Override
            public String getFilename() {
                return arquivo;
            }
        };
        MultiValueMap<String, Object> corpo = new LinkedMultiValueMap<>();
        corpo.add("file", resource);
        corpo.add("api_key", cloudinary.getApiKey());
        corpo.add("timestamp", timestamp);
        corpo.add("signature", signature);
        if (!pasta(cloudinary).isBlank()) {
            corpo.add("folder", pasta(cloudinary));
        }

        try {
            CloudinaryUploadResponse resposta = restClient.post()
                    .uri("/{cloud}/image/upload", cloudinary.getCloudName())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(corpo)
                    .retrieve()
                    .body(CloudinaryUploadResponse.class);
            if (resposta == null || resposta.secureUrl() == null || resposta.secureUrl().isBlank()) {
                throw new BadGatewayException(FALHA_UPLOAD);
            }
            return resposta.secureUrl();
        } catch (BadGatewayException | ServiceUnavailableException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new BadGatewayException(FALHA_UPLOAD);
        }
    }

    @Override
    public void excluir(String imagemUrl) {
        ApplicationProperties.Cloudinary cloudinary = propriedades.getCloudinary();
        if (!configurado(cloudinary) || !CloudinaryUrls.daConta(imagemUrl, cloudinary.getCloudName())) {
            return;
        }
        String publicId = CloudinaryUrls.publicId(imagemUrl);
        if (publicId == null) {
            return;
        }
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        Map<String, String> assinar = new LinkedHashMap<>();
        assinar.put("invalidate", "true");
        assinar.put("public_id", publicId);
        assinar.put("timestamp", timestamp);
        String signature = CloudinaryAssinatura.assinar(assinar, cloudinary.getApiSecret());

        MultiValueMap<String, String> corpo = new LinkedMultiValueMap<>();
        corpo.add("public_id", publicId);
        corpo.add("timestamp", timestamp);
        corpo.add("api_key", cloudinary.getApiKey());
        corpo.add("signature", signature);
        corpo.add("invalidate", "true");

        try {
            CloudinaryDestroyResponse resposta = restClient.post()
                    .uri("/{cloud}/image/destroy", cloudinary.getCloudName())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(corpo)
                    .retrieve()
                    .body(CloudinaryDestroyResponse.class);
            if (resposta != null
                    && resposta.result() != null
                    && !"ok".equals(resposta.result())
                    && !"not found".equals(resposta.result())) {
                throw new BadGatewayException(FALHA_EXCLUSAO);
            }
        } catch (BadGatewayException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new BadGatewayException(FALHA_EXCLUSAO);
        }
    }

    private static void exigirConfigurado(ApplicationProperties.Cloudinary cloudinary) {
        if (!configurado(cloudinary)) {
            throw new ServiceUnavailableException("Upload de imagens indisponível");
        }
    }

    private static boolean configurado(ApplicationProperties.Cloudinary cloudinary) {
        return cloudinary != null
                && notBlank(cloudinary.getCloudName())
                && notBlank(cloudinary.getApiKey())
                && notBlank(cloudinary.getApiSecret());
    }

    private static String pasta(ApplicationProperties.Cloudinary cloudinary) {
        return cloudinary.getFolder() == null ? "" : cloudinary.getFolder().trim();
    }

    private static boolean notBlank(String valor) {
        return valor != null && !valor.isBlank();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record CloudinaryUploadResponse(
            @com.fasterxml.jackson.annotation.JsonProperty("secure_url") String secureUrl
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record CloudinaryDestroyResponse(String result) {
    }
}
