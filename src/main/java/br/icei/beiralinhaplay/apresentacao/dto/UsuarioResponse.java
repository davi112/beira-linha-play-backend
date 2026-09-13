package br.icei.beiralinhaplay.apresentacao.dto;

import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;

import java.util.List;

public record UsuarioResponse(
        String id,
        String nome,
        String email,
        TipoUsuario tipo,
        List<String> cursoIds,
        String apelido,
        Integer pontos,
        String imagemPerfil,
        String cursoOrigem
) {
}
