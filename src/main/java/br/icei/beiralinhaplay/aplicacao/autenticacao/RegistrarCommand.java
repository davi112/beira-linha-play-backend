package br.icei.beiralinhaplay.aplicacao.autenticacao;

import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;

public record RegistrarCommand(
        TipoUsuario tipo,
        String nome,
        String apelido,
        String email,
        String senha,
        String cursoOrigem
) {
}
