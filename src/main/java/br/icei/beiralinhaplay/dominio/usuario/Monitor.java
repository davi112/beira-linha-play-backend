package br.icei.beiralinhaplay.dominio.usuario;

import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;

import java.util.List;
import java.util.UUID;

public class Monitor extends Usuario {

    private String cursoOrigem;

    public Monitor(
            UUID id,
            String nome,
            String email,
            String senhaHash,
            List<UUID> cursoIds,
            String cursoOrigem
    ) {
        super(id, nome, email, senhaHash, cursoIds);
        if (email() == null) {
            throw new BusinessRuleException("Informe um e-mail válido");
        }
        this.cursoOrigem = cursoOrigem;
    }

    @Override
    public TipoUsuario tipo() {
        return TipoUsuario.MONITOR;
    }

    public void definirCursoOrigem(String cursoOrigem) {
        this.cursoOrigem = cursoOrigem;
    }

    public String cursoOrigem() {
        return cursoOrigem;
    }
}
