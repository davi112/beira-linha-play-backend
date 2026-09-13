package br.icei.beiralinhaplay.apresentacao.rest;

import br.icei.beiralinhaplay.aplicacao.medalha.MedalhaService;
import br.icei.beiralinhaplay.apresentacao.dto.DtoConverter;
import br.icei.beiralinhaplay.apresentacao.dto.MedalhaResponse;
import br.icei.beiralinhaplay.apresentacao.dto.SalvarMedalhaRequest;
import br.icei.beiralinhaplay.apresentacao.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medalhas")
public class MedalhaController {

    private final MedalhaService servicoMedalha;

    public MedalhaController(MedalhaService servicoMedalha) {
        this.servicoMedalha = servicoMedalha;
    }

    @GetMapping
    public List<MedalhaResponse> listar(Authentication authentication) {
        return servicoMedalha.listar(AuthHttp.usuario(authentication))
                .stream()
                .map(DtoConverter::medalha)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedalhaResponse criar(
            Authentication authentication,
            @Valid @RequestBody SalvarMedalhaRequest requisicao
    ) {
        var medalha = servicoMedalha.criar(AuthHttp.usuario(authentication), DtoConverter.comando(requisicao));
        return new MedalhaResponse(
                DtoConverter.id(medalha.id()),
                medalha.nome(),
                medalha.imagemUrl(),
                medalha.pontosMin(),
                false
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable String id, Authentication authentication) {
        servicoMedalha.excluir(AuthHttp.usuario(authentication), DtoConverter.id(id));
    }

    @PostMapping("/{id}/equipar")
    public UsuarioResponse equipar(@PathVariable String id, Authentication authentication) {
        return DtoConverter.usuario(
                servicoMedalha.equiparAvatar(AuthHttp.usuario(authentication), DtoConverter.id(id))
        );
    }
}
