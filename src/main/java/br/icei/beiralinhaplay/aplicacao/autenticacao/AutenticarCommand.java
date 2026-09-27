package br.icei.beiralinhaplay.aplicacao.autenticacao;

import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;

public record AutenticarCommand(
        TipoUsuario tipo,
        String apelido,
        String email,
        String nome,
        String senha
) { }
