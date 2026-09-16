package br.icei.beiralinhaplay.infraestrutura.ia;

import br.icei.beiralinhaplay.aplicacao.ia.GeradorQuestoes;
import br.icei.beiralinhaplay.dominio.compartilhado.BadGatewayException;
import br.icei.beiralinhaplay.dominio.compartilhado.ServiceUnavailableException;
import br.icei.beiralinhaplay.infraestrutura.configuracao.ApplicationProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;

@Component
public class GeminiGeradorQuestoes implements GeradorQuestoes {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    private static final String FALHA_UPSTREAM = "Não foi possível gerar as questões. Tente de novo.";

    private final ApplicationProperties propriedades;
    private final RestClient restClient;

    public GeminiGeradorQuestoes(ApplicationProperties propriedades) {
        this.propriedades = propriedades;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(60));
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory(factory)
                .build();
    }

    GeminiGeradorQuestoes(ApplicationProperties propriedades, RestClient restClient) {
        this.propriedades = propriedades;
        this.restClient = restClient;
    }

    @Override
    public String gerar(String prompt, String systemInstruction) {
        String chave = propriedades.getGemini().getApiKey();
        if (chave == null || chave.isBlank()) {
            throw new ServiceUnavailableException("Geração de questões indisponível");
        }

        GeminiRequest corpo = new GeminiRequest(
                new GeminiContent(List.of(new GeminiPart(systemInstruction))),
                List.of(new GeminiContent(List.of(new GeminiPart(prompt)))),
                new GeminiGenerationConfig(
                        "application/json",
                        new GeminiThinkingConfig(0)
                )
        );

        try {
            GeminiResponse resposta = restClient.post()
                    .uri("/models/{model}:generateContent", propriedades.getGemini().getModel())
                    .header("x-goog-api-key", chave)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(corpo)
                    .retrieve()
                    .body(GeminiResponse.class);
            String texto = texto(resposta);
            if (texto == null || texto.isBlank()) {
                throw new BadGatewayException(FALHA_UPSTREAM);
            }
            return texto;
        } catch (BadGatewayException | ServiceUnavailableException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new BadGatewayException(FALHA_UPSTREAM);
        }
    }

    private static String texto(GeminiResponse resposta) {
        if (resposta == null || resposta.candidates() == null || resposta.candidates().isEmpty()) {
            return null;
        }
        GeminiContent content = resposta.candidates().getFirst().content();
        if (content == null || content.parts() == null || content.parts().isEmpty()) {
            return null;
        }
        return content.parts().getFirst().text();
    }

    private record GeminiRequest(
            @JsonProperty("system_instruction") GeminiContent systemInstruction,
            List<GeminiContent> contents,
            GeminiGenerationConfig generationConfig
    ) {
    }

    private record GeminiContent(List<GeminiPart> parts) {
    }

    private record GeminiPart(String text) {
    }

    private record GeminiGenerationConfig(
            String responseMimeType,
            GeminiThinkingConfig thinkingConfig
    ) {
    }

    private record GeminiThinkingConfig(int thinkingBudget) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiResponse(List<GeminiCandidate> candidates) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiCandidate(GeminiContent content) {
    }
}
