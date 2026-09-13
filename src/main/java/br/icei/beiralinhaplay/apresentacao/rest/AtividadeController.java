package br.icei.beiralinhaplay.apresentacao.rest;

import br.icei.beiralinhaplay.aplicacao.atividade.AtividadeService;
import br.icei.beiralinhaplay.apresentacao.dto.MonitoramentoResponse;
import br.icei.beiralinhaplay.aplicacao.tentativa.TentativaService;
import br.icei.beiralinhaplay.apresentacao.dto.AtividadeResponse;
import br.icei.beiralinhaplay.apresentacao.dto.DtoConverter;
import br.icei.beiralinhaplay.apresentacao.dto.SalvarAtividadeRequest;
import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;
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

@RestController
public class AtividadeController {

    private final AtividadeService servicoAtividade;
    private final TentativaService servicoTentativa;

    public AtividadeController(AtividadeService servicoAtividade, TentativaService servicoTentativa) {
        this.servicoAtividade = servicoAtividade;
        this.servicoTentativa = servicoTentativa;
    }

    @PostMapping("/api/modulos/{moduloId}/atividades")
    @ResponseStatus(HttpStatus.CREATED)
    public AtividadeResponse criar(
            @PathVariable String moduloId,
            Authentication authentication,
            @Valid @RequestBody SalvarAtividadeRequest requisicao
    ) {
        var atividade = servicoAtividade.criar(
                AuthHttp.usuario(authentication),
                DtoConverter.id(moduloId),
                DtoConverter.comando(requisicao)
        );
        return DtoConverter.atividade(atividade, true);
    }

    @GetMapping("/api/atividades/{id}")
    public AtividadeResponse buscar(@PathVariable String id, Authentication authentication) {
        Usuario usuario = AuthHttp.usuario(authentication);
        boolean gabarito = usuario.tipo() != TipoUsuario.ALUNO;
        return DtoConverter.atividade(servicoAtividade.buscar(DtoConverter.id(id)), gabarito);
    }

    @PatchMapping("/api/atividades/{id}")
    public AtividadeResponse atualizar(
            @PathVariable String id,
            Authentication authentication,
            @Valid @RequestBody SalvarAtividadeRequest requisicao
    ) {
        var atividade = servicoAtividade.atualizar(
                AuthHttp.usuario(authentication),
                DtoConverter.id(id),
                DtoConverter.comando(requisicao)
        );
        return DtoConverter.atividade(atividade, true);
    }

    @DeleteMapping("/api/atividades/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable String id, Authentication authentication) {
        servicoAtividade.excluir(AuthHttp.usuario(authentication), DtoConverter.id(id));
    }

    @GetMapping("/api/atividades/{id}/monitoramento")
    public MonitoramentoResponse monitoramento(@PathVariable String id, Authentication authentication) {
        return MonitoramentoResponse.de(
                servicoTentativa.monitorar(AuthHttp.usuario(authentication), DtoConverter.id(id))
        );
    }
}
