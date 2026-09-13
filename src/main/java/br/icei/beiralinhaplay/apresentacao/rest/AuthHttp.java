package br.icei.beiralinhaplay.apresentacao.rest;

import br.icei.beiralinhaplay.dominio.usuario.Usuario;
import org.springframework.security.core.Authentication;

final class AuthHttp {

    private AuthHttp() {
    }

    static Usuario usuario(Authentication authentication) {
        return (Usuario) authentication.getPrincipal();
    }
}
