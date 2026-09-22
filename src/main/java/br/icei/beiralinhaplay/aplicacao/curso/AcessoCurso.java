package br.icei.beiralinhaplay.aplicacao.curso;

import br.icei.beiralinhaplay.dominio.compartilhado.ForbiddenException;
import br.icei.beiralinhaplay.dominio.curso.Curso;
import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;

public final class AcessoCurso {

    private AcessoCurso() {}

    public static boolean visivelNaLista(Usuario usuario, Curso curso) {
        if (usuario.tipo() == TipoUsuario.ADMIN) {
            return true;
        }
        if (usuario.tipo() == TipoUsuario.MONITOR) {
            return curso.monitorIds().contains(usuario.id());
        }
        return usuario.inscritoOuMinistra(curso.id());
    }

    public static void exigirLeitura(Usuario usuario, Curso curso) {
        if (!visivelNaLista(usuario, curso)) {
            throw new ForbiddenException("Você não tem acesso a este curso");
        }
    }
}
