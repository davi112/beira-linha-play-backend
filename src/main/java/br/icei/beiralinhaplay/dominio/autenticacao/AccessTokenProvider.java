package br.icei.beiralinhaplay.dominio.autenticacao;

import br.icei.beiralinhaplay.dominio.usuario.Usuario;

import java.util.UUID;

public interface AccessTokenProvider {

    String gerar(Usuario usuario);

    ClaimsToken validar(String token);

    record ClaimsToken(UUID usuarioId, String tipo) {
    }
}
