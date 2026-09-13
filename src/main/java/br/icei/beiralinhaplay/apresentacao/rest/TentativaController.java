package br.icei.beiralinhaplay.apresentacao.rest;

import br.icei.beiralinhaplay.aplicacao.tentativa.TentativaService;
import br.icei.beiralinhaplay.apresentacao.dto.DtoConverter;
import br.icei.beiralinhaplay.apresentacao.dto.EnviarTentativaRequest;
import br.icei.beiralinhaplay.apresentacao.dto.ResultadoTentativaResponse;
import br.icei.beiralinhaplay.apresentacao.dto.TentativaResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class TentativaController {

    private final TentativaService servicoTentativa;

    public TentativaController(TentativaService servicoTentativa) {
        this.servicoTentativa = servicoTentativa;
    }

    @GetMapping("/api/alunos/me/tentativas")
    public List<TentativaResponse> minhas(
            Authentication authentication,
            @RequestParam(required = false) String atividadeId
    ) {
        UUID atividade = atividadeId == null ? null : DtoConverter.id(atividadeId);
        return servicoTentativa.listarDoAluno(AuthHttp.usuario(authentication).id(), atividade)
                .stream()
                .map(DtoConverter::tentativa)
                .toList();
    }

    @PostMapping("/api/atividades/{atividadeId}/tentativas")
    @ResponseStatus(HttpStatus.CREATED)
    public ResultadoTentativaResponse enviar(
            @PathVariable String atividadeId,
            Authentication authentication,
            @Valid @RequestBody EnviarTentativaRequest requisicao
    ) {
        return DtoConverter.resultado(servicoTentativa.enviar(
                AuthHttp.usuario(authentication),
                DtoConverter.id(atividadeId),
                DtoConverter.comando(requisicao)
        ));
    }
}
