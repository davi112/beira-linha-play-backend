package br.icei.beiralinhaplay.infraestrutura.integracoes.sympla;

import br.icei.beiralinhaplay.dominio.usuario.Aluno;
import br.icei.beiralinhaplay.infraestrutura.configuracao.ApplicationProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

public class ImportadorParticipantesSympla {

    private static final String BASE_URL = "https://api.sympla.com.br/public/v1.6.0";
    private final RestClient restClient;
    private final ApplicationProperties applicationProperties;

    public ImportadorParticipantesSympla(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(60));
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory(factory)
                .build();
    }

    public List<Aluno> importar(String idEventoSympla){
        ApplicationProperties.Sympla sympla = applicationProperties.getSympla();
        String token = applicationProperties.getSympla().getToken();

        restClient.get()
                    .uri("/events/{idEventoSympla}/participants", idEventoSympla)
                    .header("s_token", token)
                    .header("Accept", "application/json")
                    .retrieve();
        return null;
    }

}
