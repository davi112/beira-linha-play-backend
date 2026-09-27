package br.icei.beiralinhaplay.apresentacao.rest;

import br.icei.beiralinhaplay.aplicacao.usuario.ContaService;
import br.icei.beiralinhaplay.aplicacao.usuario.ImportarInscritosService;
import br.icei.beiralinhaplay.apresentacao.dto.DtoConverter;
import br.icei.beiralinhaplay.apresentacao.dto.AtualizarContaRequest;
import br.icei.beiralinhaplay.apresentacao.dto.CriarAdminRequest;
import br.icei.beiralinhaplay.apresentacao.dto.AlunoResumoResponse;
import br.icei.beiralinhaplay.apresentacao.dto.EventoDisponivelResponse;
import br.icei.beiralinhaplay.apresentacao.dto.ImportarInscritosRequest;
import br.icei.beiralinhaplay.apresentacao.dto.LogImportacaoResponse;
import br.icei.beiralinhaplay.apresentacao.dto.UsuarioResponse;
import br.icei.beiralinhaplay.aplicacao.usuario.ImportarInscritosService.LogImportacaoConsulta;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final ImportarInscritosService importarInscritosService;

    public UsuarioController(ContaService servicoConta, ImportarInscritosService importarInscritosService) {
        this.servicoConta = servicoConta;
        this.importarInscritosService = importarInscritosService;
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

    @GetMapping("/importacao/eventos")
    public List<EventoDisponivelResponse> listarEventos(
            Authentication authentication,
            @RequestParam int ano
    ) {
        return importarInscritosService.listarEventos(AuthHttp.usuario(authentication), ano)
                .stream()
                .map(evento -> new EventoDisponivelResponse(
                        evento.referencia(),
                        evento.nome(),
                        evento.inicio(),
                        evento.fim()
                ))
                .toList();
    }

    @GetMapping("/importacao/logs")
    public List<LogImportacaoResponse> listarLogs(Authentication authentication) {
        return importarInscritosService.listarLogs(AuthHttp.usuario(authentication)).stream()
                .map(UsuarioController::log)
                .toList();
    }

    @GetMapping("/importacao/logs/{id}/alunos")
    public List<AlunoResumoResponse> alunosDoLog(
            @PathVariable String id,
            Authentication authentication
    ) {
        return importarInscritosService
                .listarAlunosDoLog(AuthHttp.usuario(authentication), DtoConverter.id(id))
                .stream()
                .map(aluno -> new AlunoResumoResponse(aluno.nome(), aluno.email(), aluno.apelido()))
                .toList();
    }

    @PostMapping("/importacao/inscritos")
    public LogImportacaoResponse importarUsuarios(
            Authentication authentication,
            @Valid @RequestBody ImportarInscritosRequest requisicao
    ) {
        return log(importarInscritosService.importarInscritos(
                AuthHttp.usuario(authentication),
                requisicao.referencia()
        ));
    }

    private static LogImportacaoResponse log(LogImportacaoConsulta consulta) {
        return new LogImportacaoResponse(
                consulta.id().toString(),
                consulta.nomeEvento(),
                consulta.urlEvento(),
                consulta.quantidadeAlunos(),
                consulta.quantidadeCursos(),
                consulta.dataImportacao(),
                consulta.adminNome()
        );
    }
}
