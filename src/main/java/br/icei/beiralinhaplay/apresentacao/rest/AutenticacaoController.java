package br.icei.beiralinhaplay.apresentacao.rest;

import br.icei.beiralinhaplay.aplicacao.autenticacao.AutenticacaoResult;
import br.icei.beiralinhaplay.aplicacao.autenticacao.AutenticacaoService;
import br.icei.beiralinhaplay.apresentacao.dto.DtoConverter;
import br.icei.beiralinhaplay.apresentacao.dto.CadastroRequest;
import br.icei.beiralinhaplay.apresentacao.dto.LoginRequest;
import br.icei.beiralinhaplay.apresentacao.dto.UsuarioResponse;
import br.icei.beiralinhaplay.infraestrutura.seguranca.AuthCookieManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    private final AutenticacaoService servicoAutenticacao;
    private final AuthCookieManager cookies;

    public AutenticacaoController(
            AutenticacaoService servicoAutenticacao,
            AuthCookieManager cookies
    ) {
        this.servicoAutenticacao = servicoAutenticacao;
        this.cookies = cookies;
    }

    @PostMapping("/login")
    public UsuarioResponse login(
            @Valid @RequestBody LoginRequest requisicao,
            HttpServletResponse response
    ) {
        return gravar(servicoAutenticacao.autenticar(DtoConverter.comando(requisicao)), response);
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastro(
            @Valid @RequestBody CadastroRequest requisicao,
            HttpServletResponse response
    ) {
        return gravar(servicoAutenticacao.registrar(DtoConverter.comando(requisicao)), response);
    }

    @PostMapping("/refresh")
    public UsuarioResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        return gravar(servicoAutenticacao.renovar(cookie(request, cookies.nomeRefresh())), response);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        servicoAutenticacao.encerrar(cookie(request, cookies.nomeRefresh()));
        cookies.limpar(response);
    }

    @GetMapping("/me")
    public UsuarioResponse me(Authentication authentication) {
        return DtoConverter.usuario(AuthHttp.usuario(authentication));
    }

    private UsuarioResponse gravar(AutenticacaoResult resultado, HttpServletResponse response) {
        cookies.gravar(response, resultado.tokenAcesso(), resultado.tokenAtualizacao());
        return DtoConverter.usuario(resultado.usuario());
    }

    private static String cookie(HttpServletRequest request, String nome) {
        Cookie[] cookiesReq = request.getCookies();
        if (cookiesReq == null) {
            return null;
        }
        for (Cookie cookie : cookiesReq) {
            if (nome.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
