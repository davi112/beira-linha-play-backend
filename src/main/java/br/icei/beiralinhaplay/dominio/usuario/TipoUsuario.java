package br.icei.beiralinhaplay.dominio.usuario;

import java.util.List;

public enum TipoUsuario {
    ALUNO,
    MONITOR,
    ADMIN;

    public static List<TipoUsuario> aptosGerenciamentoCursos(){
        return List.of(new TipoUsuario[]{MONITOR, ADMIN});
    }
}
