package br.icei.beiralinhaplay.infraestrutura.seguranca;

import br.icei.beiralinhaplay.infraestrutura.configuracao.ApplicationProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieManager {

    private final ApplicationProperties propriedades;

    public AuthCookieManager(ApplicationProperties propriedades) {
        this.propriedades = propriedades;
    }

    public void gravar(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookieAcesso(accessToken).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieRefresh(refreshToken).toString());
    }

    public void limpar(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, base(nomeAccess(), "")
                .path("/")
                .maxAge(Duration.ZERO)
                .build()
                .toString());
        response.addHeader(HttpHeaders.SET_COOKIE, base(nomeRefresh(), "")
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .build()
                .toString());
    }

    public String nomeAccess() {
        return propriedades.getCookie().getAccessName();
    }

    public String nomeRefresh() {
        return propriedades.getCookie().getRefreshName();
    }

    private ResponseCookie cookieAcesso(String valor) {
        return base(propriedades.getCookie().getAccessName(), valor)
                .path("/")
                .maxAge(Duration.ofMinutes(propriedades.getJwt().getAccessTokenMinutos()))
                .build();
    }

    private ResponseCookie cookieRefresh(String valor) {
        return base(propriedades.getCookie().getRefreshName(), valor)
                .path("/api/auth")
                .maxAge(Duration.ofDays(propriedades.getJwt().getRefreshTokenDias()))
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String nome, String valor) {
        return ResponseCookie.from(nome, valor == null ? "" : valor)
                .httpOnly(true)
                .secure(propriedades.getCookie().isSecure())
                .sameSite(propriedades.getCookie().getSameSite());
    }
}
