package br.icei.beiralinhaplay.apresentacao.rest;

import br.icei.beiralinhaplay.aplicacao.atividade.AtividadeService;
import br.icei.beiralinhaplay.aplicacao.modulo.ModuloService;
import br.icei.beiralinhaplay.apresentacao.dto.DtoConverter;
import br.icei.beiralinhaplay.apresentacao.dto.ModuloResponse;
import br.icei.beiralinhaplay.apresentacao.dto.SalvarModuloRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ModuloController {

    private final ModuloService servicoModulo;
    private final AtividadeService servicoAtividade;

    public ModuloController(ModuloService servicoModulo, AtividadeService servicoAtividade) {
        this.servicoModulo = servicoModulo;
        this.servicoAtividade = servicoAtividade;
    }

    @PostMapping("/api/cursos/{cursoId}/modulos")
    @ResponseStatus(HttpStatus.CREATED)
    public ModuloResponse criar(
            @PathVariable String cursoId,
            Authentication authentication,
            @Valid @RequestBody SalvarModuloRequest requisicao
    ) {
        var modulo = servicoModulo.criar(
                AuthHttp.usuario(authentication),
                DtoConverter.id(cursoId),
                DtoConverter.comando(requisicao)
        );
        return DtoConverter.modulo(modulo, List.of());
    }

    @GetMapping("/api/modulos/{id}")
    public ModuloResponse buscar(@PathVariable String id) {
        var modulo = servicoModulo.buscar(DtoConverter.id(id));
        return DtoConverter.modulo(modulo, servicoAtividade.listarPorModulo(modulo.id()));
    }

    @PatchMapping("/api/modulos/{id}")
    public ModuloResponse atualizar(
            @PathVariable String id,
            Authentication authentication,
            @Valid @RequestBody SalvarModuloRequest requisicao
    ) {
        var modulo = servicoModulo.atualizar(
                AuthHttp.usuario(authentication),
                DtoConverter.id(id),
                DtoConverter.comando(requisicao)
        );
        return DtoConverter.modulo(modulo, servicoAtividade.listarPorModulo(modulo.id()));
    }

    @DeleteMapping("/api/modulos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable String id, Authentication authentication) {
        servicoModulo.excluir(AuthHttp.usuario(authentication), DtoConverter.id(id));
    }
}
