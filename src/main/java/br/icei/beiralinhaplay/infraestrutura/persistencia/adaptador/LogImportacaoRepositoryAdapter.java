package br.icei.beiralinhaplay.infraestrutura.persistencia.adaptador;

import br.icei.beiralinhaplay.dominio.importacao.LogImportacao;
import br.icei.beiralinhaplay.dominio.importacao.LogImportacaoRepository;
import br.icei.beiralinhaplay.dominio.usuario.Aluno;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.LogImportacaoEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.repositorios.AlunoJpaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.repositorios.LogImportacaoJpaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.mapeamento.UsuarioMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class LogImportacaoRepositoryAdapter implements LogImportacaoRepository {

    private final LogImportacaoJpaRepository jpaRepository;
    private final AlunoJpaRepository alunoRepository;

    public LogImportacaoRepositoryAdapter(
            LogImportacaoJpaRepository jpaRepository,
            AlunoJpaRepository alunoRepository
    ) {
        this.jpaRepository = jpaRepository;
        this.alunoRepository = alunoRepository;
    }

    @Override
    public LogImportacao salvar(LogImportacao log) {
        LogImportacaoEntity jpa = new LogImportacaoEntity();
        jpa.setNomeEvento(limitar(log.nomeEvento(), 200));
        jpa.setUrlEvento(limitar(log.urlEvento(), 500));
        jpa.setQuantidadeAlunos(log.quantidadeAlunos());
        jpa.setQuantidadeCursos(log.quantidadeCursos());
        jpa.setDataImportacao(log.dataImportacao());
        jpa.setAdminId(log.adminId());
        LogImportacaoEntity salvo = jpaRepository.save(jpa);
        return paraDominio(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogImportacao> listar() {
        return jpaRepository.findAllByOrderByDataImportacaoDesc().stream()
                .map(LogImportacaoRepositoryAdapter::paraDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Aluno> listarAlunos(UUID logId) {
        return alunoRepository.findByLogImportacaoId(logId).stream()
                .map(UsuarioMapper::paraDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existe(UUID id) {
        return id != null && jpaRepository.existsById(id);
    }

    private static LogImportacao paraDominio(LogImportacaoEntity jpa) {
        return new LogImportacao(
                jpa.getId(),
                jpa.getNomeEvento(),
                jpa.getUrlEvento(),
                jpa.getQuantidadeAlunos(),
                jpa.getQuantidadeCursos(),
                jpa.getDataImportacao(),
                jpa.getAdminId()
        );
    }

    private static String limitar(String valor, int maximo) {
        if (valor == null || valor.isBlank()) {
            return valor == null ? "" : valor;
        }
        String texto = valor.trim();
        return texto.length() <= maximo ? texto : texto.substring(0, maximo);
    }
}
