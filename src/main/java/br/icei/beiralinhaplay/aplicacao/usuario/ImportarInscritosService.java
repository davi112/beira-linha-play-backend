package br.icei.beiralinhaplay.aplicacao.usuario;

import br.icei.beiralinhaplay.dominio.compartilhado.ForbiddenException;
import br.icei.beiralinhaplay.dominio.usuario.Aluno;
import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;
import br.icei.beiralinhaplay.infraestrutura.integracoes.sympla.ImportadorParticipantesSympla;

import java.util.List;

public class ImportarInscritosService {

    private final ImportadorParticipantesSympla importadorParticipantes;

    public ImportarInscritosService(ImportadorParticipantesSympla importadorParticipantes) {
        this.importadorParticipantes = importadorParticipantes;
    }

    public List<Aluno> importarInscritos(Usuario solicitante, String idEventoExterno){
        if (solicitante == null || solicitante.tipo() != TipoUsuario.ADMIN) {
            throw new ForbiddenException("Somente administradores importam incritos");
        }

        return importadorParticipantes.importar(idEventoExterno);
    }
}
