package br.icei.beiralinhaplay.aplicacao.autenticacao;

import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;

public record AutenticacaoResult(Usuario usuario, String tokenAcesso, String tokenAtualizacao) {

    public TipoUsuario tipo() {
        return usuario.tipo();
    }
}
