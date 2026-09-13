package br.icei.beiralinhaplay.dominio.usuario;

import java.util.List;
import java.util.UUID;

public class Admin extends Usuario {

    public Admin(UUID id, String nome, String email, String senhaHash) {
        super(id, nome, email, senhaHash, List.of());
    }

    @Override
    public TipoUsuario tipo() {
        return TipoUsuario.ADMIN;
    }

    @Override
    public void adicionarCurso(UUID cursoId) {
        // Admin não se inscreve nem ministra cursos.
    }

    @Override
    public void substituirCursos(List<UUID> ids) {
        super.substituirCursos(List.of());
    }
}
