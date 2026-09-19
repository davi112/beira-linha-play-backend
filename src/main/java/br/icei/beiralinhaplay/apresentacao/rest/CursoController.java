package br.icei.beiralinhaplay.apresentacao.rest;

import br.icei.beiralinhaplay.aplicacao.curso.CursoService;
import br.icei.beiralinhaplay.aplicacao.modulo.ModuloService;
import br.icei.beiralinhaplay.apresentacao.dto.DtoConverter;
import br.icei.beiralinhaplay.apresentacao.dto.CursoResponse;
import br.icei.beiralinhaplay.apresentacao.dto.InscreverCursoRequest;
import br.icei.beiralinhaplay.apresentacao.dto.SalvarCursoRequest;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService servicoCurso;
    private final ModuloService servicoModulo;

    public CursoController(CursoService servicoCurso, ModuloService servicoModulo) {
        this.servicoCurso = servicoCurso;
        this.servicoModulo = servicoModulo;
    }

    @GetMapping
    public List<CursoResponse> listar(Authentication authentication) {
        Usuario usuario = AuthHttp.usuario(authentication);
        boolean codigo = usuario.tipo() != TipoUsuario.ALUNO;
        var nomes = servicoCurso.mapaNomesMonitores();
        return servicoCurso.listar(usuario).stream()
                .map(curso -> DtoConverter.curso(curso, servicoModulo.listarPorCurso(curso.id()), codigo, nomes))
                .toList();
    }

    @GetMapping("/{id}")
    public CursoResponse buscar(@PathVariable String id, Authentication authentication) {
        Usuario usuario = AuthHttp.usuario(authentication);
        var curso = servicoCurso.buscar(usuario, DtoConverter.id(id));
        boolean codigo = usuario.tipo() != TipoUsuario.ALUNO;
        return DtoConverter.curso(
                curso,
                servicoModulo.listarPorCurso(curso.id()),
                codigo,
                servicoCurso.mapaNomesMonitores()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CursoResponse criar(
            Authentication authentication,
            @Valid @RequestBody SalvarCursoRequest requisicao
    ) {
        var curso = servicoCurso.criar(AuthHttp.usuario(authentication), DtoConverter.comando(requisicao));
        return DtoConverter.curso(curso, List.of(), true, servicoCurso.mapaNomesMonitores());
    }

    @PatchMapping("/{id}")
    public CursoResponse atualizar(
            @PathVariable String id,
            Authentication authentication,
            @Valid @RequestBody SalvarCursoRequest requisicao
    ) {
        var curso = servicoCurso.atualizar(
                AuthHttp.usuario(authentication),
                DtoConverter.id(id),
                DtoConverter.comando(requisicao)
        );
        return DtoConverter.curso(
                curso,
                servicoModulo.listarPorCurso(curso.id()),
                true,
                servicoCurso.mapaNomesMonitores()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable String id, Authentication authentication) {
        servicoCurso.excluir(AuthHttp.usuario(authentication), DtoConverter.id(id));
    }

    @PostMapping("/inscrever")
    public CursoResponse inscreverPorCodigo(
            Authentication authentication,
            @Valid @RequestBody InscreverCursoRequest requisicao
    ) {
        var curso = servicoCurso.inscreverPorCodigo(
                AuthHttp.usuario(authentication),
                requisicao.codigoAcesso()
        );
        return DtoConverter.curso(
                curso,
                servicoModulo.listarPorCurso(curso.id()),
                false,
                servicoCurso.mapaNomesMonitores()
        );
    }

    @PostMapping("/{id}/inscrever")
    public CursoResponse inscrever(
            @PathVariable String id,
            Authentication authentication,
            @Valid @RequestBody InscreverCursoRequest requisicao
    ) {
        var curso = servicoCurso.inscrever(
                AuthHttp.usuario(authentication),
                DtoConverter.id(id),
                requisicao.codigoAcesso()
        );
        return DtoConverter.curso(
                curso,
                servicoModulo.listarPorCurso(curso.id()),
                false,
                servicoCurso.mapaNomesMonitores()
        );
    }
}
