package br.icei.beiralinhaplay.apresentacao.rest;

import br.icei.beiralinhaplay.aplicacao.usuario.ContaService;
import br.icei.beiralinhaplay.apresentacao.dto.DtoConverter;
import br.icei.beiralinhaplay.apresentacao.dto.AtualizarContaRequest;
import br.icei.beiralinhaplay.apresentacao.dto.CriarAdminRequest;
import br.icei.beiralinhaplay.apresentacao.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    private final ContaService servicoConta;

    public UsuarioController(ContaService servicoConta) {
        this.servicoConta = servicoConta;
    }

    @PatchMapping("/usuarios/me")
    public UsuarioResponse atualizar(
            Authentication authentication,
            @Valid @RequestBody AtualizarContaRequest requisicao
    ) {
        return DtoConverter.usuario(
                servicoConta.atualizar(AuthHttp.usuario(authentication).id(), DtoConverter.comando(requisicao))
        );
    }

    @PostMapping("/admins")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criarAdmin(
            Authentication authentication,
            @Valid @RequestBody CriarAdminRequest requisicao
    ) {
        return DtoConverter.usuario(
                servicoConta.criarAdmin(AuthHttp.usuario(authentication), DtoConverter.comando(requisicao))
        );
    }

    @GetMapping("/monitores")
    public List<UsuarioResponse> monitores(Authentication authentication) {
        return servicoConta.listarMonitores(AuthHttp.usuario(authentication))
                .stream()
                .map(DtoConverter::usuario)
                .toList();
    }
}
