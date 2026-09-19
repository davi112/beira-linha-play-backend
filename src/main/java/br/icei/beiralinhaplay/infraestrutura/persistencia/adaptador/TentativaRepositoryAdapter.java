package br.icei.beiralinhaplay.infraestrutura.persistencia.adaptador;

import br.icei.beiralinhaplay.dominio.resposta.Resposta;
import br.icei.beiralinhaplay.dominio.tentativa.TentativaRepository;
import br.icei.beiralinhaplay.dominio.tentativa.Tentativa;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.AlunoJpaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.AlternativaJpaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.AtividadeJpaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.QuestaoJpaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.RespostaEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.TentativaEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.TentativaJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public class TentativaRepositoryAdapter implements TentativaRepository {

    private final TentativaJpaRepository tentativaJpaRepository;
    private final AlunoJpaRepository alunoJpaRepository;
    private final AtividadeJpaRepository atividadeJpaRepository;
    private final QuestaoJpaRepository questaoJpaRepository;
    private final AlternativaJpaRepository alternativaJpaRepository;

    public TentativaRepositoryAdapter(
            TentativaJpaRepository tentativaJpaRepository,
            AlunoJpaRepository alunoJpaRepository,
            AtividadeJpaRepository atividadeJpaRepository,
            QuestaoJpaRepository questaoJpaRepository,
            AlternativaJpaRepository alternativaJpaRepository
    ) {
        this.tentativaJpaRepository = tentativaJpaRepository;
        this.alunoJpaRepository = alunoJpaRepository;
        this.atividadeJpaRepository = atividadeJpaRepository;
        this.questaoJpaRepository = questaoJpaRepository;
        this.alternativaJpaRepository = alternativaJpaRepository;
    }

    @Override
    public Tentativa salvar(Tentativa tentativa) {
        TentativaEntity jpa = new TentativaEntity();
        jpa.setDataEnvio(tentativa.dataEnvio());
        jpa.setPontuacaoObtida(tentativa.pontuacaoObtida());
        jpa.setAluno(alunoJpaRepository.getReferenceById(tentativa.alunoId()));
        jpa.setAtividade(atividadeJpaRepository.getReferenceById(tentativa.atividadeId()));
        for (Resposta resposta : tentativa.respostas()) {
            RespostaEntity respostaJpa = new RespostaEntity();
            respostaJpa.setCorreta(resposta.correta());
            respostaJpa.setTentativa(jpa);
            respostaJpa.setQuestao(questaoJpaRepository.getReferenceById(resposta.questaoId()));
            respostaJpa.setAlternativa(alternativaJpaRepository.getReferenceById(resposta.alternativaId()));
            jpa.getRespostas().add(respostaJpa);
        }
        TentativaEntity salvo = tentativaJpaRepository.save(jpa);
        return paraDominioPreservandoCorrecao(salvo, tentativa);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tentativa> listarPorAlunoEAtividade(UUID alunoId, UUID atividadeId) {
        return tentativaJpaRepository.findByAlunoIdAndAtividadeIdOrderByDataEnvioAsc(alunoId, atividadeId)
                .stream()
                .map(TentativaRepositoryAdapter::paraDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tentativa> listarPorAtividade(UUID atividadeId) {
        return tentativaJpaRepository.findByAtividadeIdOrderByDataEnvioAsc(atividadeId)
                .stream()
                .map(TentativaRepositoryAdapter::paraDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tentativa> listarPorAluno(UUID alunoId) {
        return tentativaJpaRepository.findByAlunoIdOrderByDataEnvioDesc(alunoId)
                .stream()
                .map(TentativaRepositoryAdapter::paraDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tentativa> buscarPorId(UUID id) {
        return tentativaJpaRepository.buscarCompleto(id).map(TentativaRepositoryAdapter::paraDominio);
    }

    private static Tentativa paraDominioPreservandoCorrecao(
            TentativaEntity jpa,
            Tentativa original
    ) {
        var corretaPorQuestao = original.respostas().stream()
                .collect(java.util.stream.Collectors.toMap(Resposta::questaoId, Resposta::correta));
        List<Resposta> respostas = jpa.getRespostas().stream()
                .map(r -> new Resposta(
                        r.getId(),
                        corretaPorQuestao.getOrDefault(r.getQuestao().getId(), r.isCorreta()),
                        r.getQuestao().getId(),
                        r.getAlternativa().getId()
                ))
                .toList();
        return new Tentativa(
                jpa.getId(),
                jpa.getDataEnvio(),
                original.pontuacaoObtida(),
                jpa.getAluno().getId(),
                jpa.getAtividade().getId(),
                respostas
        );
    }

    private static Tentativa paraDominio(TentativaEntity jpa) {
        List<Resposta> respostas = jpa.getRespostas().stream()
                .map(r -> new Resposta(r.getId(), r.isCorreta(), r.getQuestao().getId(), r.getAlternativa().getId()))
                .toList();
        return new Tentativa(
                jpa.getId(),
                jpa.getDataEnvio(),
                jpa.getPontuacaoObtida(),
                jpa.getAluno().getId(),
                jpa.getAtividade().getId(),
                respostas
        );
    }
}
