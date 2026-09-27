package br.icei.beiralinhaplay.infraestrutura.integracoes.sympla;

import br.icei.beiralinhaplay.dominio.compartilhado.BadGatewayException;
import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;
import br.icei.beiralinhaplay.dominio.compartilhado.ServiceUnavailableException;
import br.icei.beiralinhaplay.infraestrutura.configuracao.ApplicationProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ImportadorParticipantesSympla {

    private static final String BASE_URL = "https://api.sympla.com.br/public/v1.6.0";
    private static final String FALHA_UPSTREAM = "Não foi possível importar os participantes do Sympla. Tente de novo.";
    private static final String STATUS_APROVADO = "APPROVED";
    private static final int TAMANHO_PAGINA = 200;
    private static final String CAMPOS_PARTICIPANTE = "first_name,last_name,email,order_status,ticket_name";
    private static final String CAMPOS_EVENTO = "id,start_date,end_date,name,url";

    private final ApplicationProperties propriedades;
    private final RestClient restClient;

    public ImportadorParticipantesSympla(ApplicationProperties propriedades) {
        this.propriedades = propriedades;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(60));
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory(factory)
                .build();
    }

    public List<Inscrito> importar(String idEventoSympla) {
        if (idEventoSympla == null || idEventoSympla.isBlank()) {
            throw new BusinessRuleException("Informe o evento do Sympla");
        }
        String token = propriedades.getSympla().getToken();
        if (token == null || token.isBlank()) {
            throw new ServiceUnavailableException("Importação do Sympla indisponível");
        }

        List<Inscrito> inscritos = new ArrayList<>();
        String cursor = null;
        while (true) {
            SymplaParticipantesResponse resposta = buscar(token, idEventoSympla.trim(), cursor);
            if (resposta == null || resposta.data() == null) {
                throw new BadGatewayException(FALHA_UPSTREAM);
            }
            for (Participante participante : resposta.data()) {
                Inscrito inscrito = paraInscrito(participante);
                if (inscrito != null) {
                    inscritos.add(inscrito);
                }
            }
            String proximo = resposta.pagination() == null ? null : resposta.pagination().nextCursor();
            if (proximo == null || proximo.isBlank() || proximo.equals(cursor)) {
                break;
            }
            cursor = proximo;
        }
        return inscritos;
    }

    public List<Evento> listarEventos(int ano) {
        String token = token();
        LocalDate inicio = LocalDate.of(ano, 1, 1);
        List<Evento> eventos = new ArrayList<>();
        String cursor = null;
        while (true) {
            SymplaEventosResponse resposta = buscarEventos(token, inicio, cursor);
            if (resposta == null || resposta.data() == null) {
                throw new BadGatewayException(FALHA_UPSTREAM);
            }
            for (EventoSympla evento : resposta.data()) {
                if (evento == null || evento.id() == null || evento.id().isBlank()) {
                    continue;
                }
                if (evento.startDate() != null && !evento.startDate().startsWith(String.valueOf(ano))) {
                    continue;
                }
                eventos.add(new Evento(
                        evento.id().trim(),
                        evento.name() == null ? "" : evento.name().trim(),
                        evento.url() == null ? "" : evento.url().trim(),
                        evento.startDate(),
                        evento.endDate()
                ));
            }
            String proximo = resposta.pagination() == null ? null : resposta.pagination().nextCursor();
            if (proximo == null || proximo.isBlank() || proximo.equals(cursor)) {
                break;
            }
            cursor = proximo;
        }
        return eventos;
    }

    public Evento buscarEvento(String idEventoSympla) {
        if (idEventoSympla == null || idEventoSympla.isBlank()) {
            throw new BusinessRuleException("Informe o evento do Sympla");
        }
        try {
            SymplaEventoResponse resposta = restClient.get()
                    .uri(uri -> uri
                            .path("/events/{idEvento}")
                            .queryParam("fields", CAMPOS_EVENTO)
                            .build(idEventoSympla.trim()))
                    .header("s_token", token())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(SymplaEventoResponse.class);
            EventoSympla evento = resposta == null ? null : resposta.data();
            if (evento == null || evento.id() == null || evento.id().isBlank()) {
                throw new BusinessRuleException("Evento inválido");
            }
            return new Evento(
                    evento.id().trim(),
                    evento.name() == null ? "" : evento.name().trim(),
                    evento.url() == null ? "" : evento.url().trim(),
                    evento.startDate(),
                    evento.endDate()
            );
        } catch (BusinessRuleException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new BadGatewayException("Não foi possível consultar o evento do Sympla. Tente de novo.");
        }
    }

    private String token() {
        String token = propriedades.getSympla().getToken();
        if (token == null || token.isBlank()) {
            throw new ServiceUnavailableException("Importação do Sympla indisponível");
        }
        return token;
    }

    private SymplaParticipantesResponse buscar(String token, String idEvento, String cursor) {
        try {
            return restClient.get()
                    .uri(uri -> {
                        var builder = uri
                                .path("/events/{idEvento}/participants")
                                .queryParam("page_size", TAMANHO_PAGINA)
                                .queryParam("fields", CAMPOS_PARTICIPANTE);
                        if (cursor != null && !cursor.isBlank()) {
                            builder.queryParam("cursor", cursor);
                        }
                        return builder.build(idEvento);
                    })
                    .header("s_token", token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(SymplaParticipantesResponse.class);
        } catch (RestClientException ex) {
            throw new BadGatewayException(FALHA_UPSTREAM);
        }
    }

    private SymplaEventosResponse buscarEventos(String token, LocalDate inicio, String cursor) {
        try {
            return restClient.get()
                    .uri(uri -> {
                        var builder = uri
                                .path("/events")
                                .queryParam("published", true)
                                .queryParam("from", inicio.toString())
                                .queryParam("page_size", TAMANHO_PAGINA)
                                .queryParam("fields", CAMPOS_EVENTO);
                        if (cursor != null && !cursor.isBlank()) {
                            builder.queryParam("cursor", cursor);
                        }
                        return builder.build();
                    })
                    .header("s_token", token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(SymplaEventosResponse.class);
        } catch (RestClientException ex) {
            throw new BadGatewayException("Não foi possível listar os eventos do Sympla. Tente de novo.");
        }
    }

    private static Inscrito paraInscrito(Participante participante) {
        if (participante == null) {
            return null;
        }
        if (participante.orderStatus() != null && !STATUS_APROVADO.equalsIgnoreCase(participante.orderStatus())) {
            return null;
        }
        String nome = nome(participante.firstName(), participante.lastName());
        if (nome.isBlank()) {
            return null;
        }
        String email = participante.email() == null ? null : participante.email().trim();
        String nomeCurso = nomeCurso(participante);
        return new Inscrito(nome, email, apelido(nome), nomeCurso);
    }

    private static String nomeCurso(Participante participante) {
        if (participante.ticketName() != null && !participante.ticketName().isBlank()) {
            return participante.ticketName().trim();
        }
        if (participante.ticket() != null && participante.ticket().name() != null) {
            return participante.ticket().name().trim();
        }
        return "";
    }

    private static String nome(String primeiro, String ultimo) {
        StringBuilder nome = new StringBuilder();
        if (primeiro != null && !primeiro.isBlank()) {
            nome.append(primeiro.trim());
        }
        if (ultimo != null && !ultimo.isBlank()) {
            if (!nome.isEmpty()) {
                nome.append(' ');
            }
            nome.append(ultimo.trim());
        }
        return nome.toString();
    }

    private static String apelido(String nome) {
        int espaco = nome.indexOf(' ');
        String primeiro = espaco > 0 ? nome.substring(0, espaco) : nome;
        return primeiro.toLowerCase(Locale.ROOT);
    }

    public record Inscrito(String nome, String email, String apelido, String nomeCurso) {
    }

    public record Evento(String id, String nome, String url, String inicio, String fim) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SymplaParticipantesResponse(List<Participante> data, Paginacao pagination) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Paginacao(
            @JsonProperty("nextCursor") @JsonAlias("next_cursor") String nextCursor
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SymplaEventosResponse(List<EventoSympla> data, Paginacao pagination) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SymplaEventoResponse(EventoSympla data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record EventoSympla(
            String id,
            String name,
            String url,
            @JsonProperty("start_date") String startDate,
            @JsonProperty("end_date") String endDate
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Participante(
            @JsonProperty("first_name") String firstName,
            @JsonProperty("last_name") String lastName,
            String email,
            @JsonProperty("order_status") String orderStatus,
            @JsonProperty("ticket_name") @JsonAlias("ticketName") String ticketName,
            Ticket ticket
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Ticket(String name) {
    }
}
