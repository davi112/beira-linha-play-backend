package br.icei.beiralinhaplay.dominio.importacao;

import br.icei.beiralinhaplay.dominio.usuario.Aluno;

import java.util.List;
import java.util.UUID;

public interface LogImportacaoRepository {

    LogImportacao salvar(LogImportacao log);

    List<LogImportacao> listar();

    List<Aluno> listarAlunos(UUID logId);

    boolean existe(UUID id);
}
