package br.icei.beiralinhaplay.infraestrutura.seguranca;

import br.icei.beiralinhaplay.dominio.autenticacao.AccessTokenProvider;
import br.icei.beiralinhaplay.dominio.usuario.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final AccessTokenProvider provedorTokenAcesso;
    private final UsuarioRepository repositorioUsuario;
    private final AuthCookieManager cookies;

    public JwtFilter(
            AccessTokenProvider provedorTokenAcesso,
            UsuarioRepository repositorioUsuario,
            AuthCookieManager cookies
    ) {
        this.provedorTokenAcesso = provedorTokenAcesso;
        this.repositorioUsuario = repositorioUsuario;
        this.cookies = cookies;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = lerCookie(request, cookies.nomeAccess());
        if (token != null && !token.isBlank()) {
            try {
                AccessTokenProvider.ClaimsToken claims = provedorTokenAcesso.validar(token);
                repositorioUsuario
                        .buscarNaoExpiradoPorId(claims.usuarioId(), LocalDate.now(ZoneOffset.UTC))
                        .ifPresent(usuario -> SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(
                                        usuario,
                                        null,
                                        List.of(new SimpleGrantedAuthority("ROLE_" + usuario.tipo().name()))
                                )
                        ));
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private static String lerCookie(HttpServletRequest request, String nome) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (nome.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
