package br.icei.beiralinhaplay.infraestrutura.persistencia.mapeamento;

import br.icei.beiralinhaplay.dominio.alternativa.Alternativa;
import br.icei.beiralinhaplay.dominio.atividade.Atividade;
import br.icei.beiralinhaplay.dominio.curso.Curso;
import br.icei.beiralinhaplay.dominio.modulo.Modulo;
import br.icei.beiralinhaplay.dominio.questao.Questao;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.AlternativaEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.AtividadeEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.CursoEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.ModuloEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.MonitorEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.QuestaoEntity;

import java.util.List;
import java.util.UUID;

public final class ConteudoMapper {

    private ConteudoMapper() {
    }

    public static Curso paraDominio(CursoEntity jpa) {
        List<UUID> monitorIds = jpa.getMonitores().stream().map(MonitorEntity::getId).toList();
        List<UUID> moduloIds = jpa.getModulos().stream().map(ModuloEntity::getId).toList();
        return new Curso(jpa.getId(), jpa.getNome(), jpa.getCodigoAcesso(), monitorIds, moduloIds);
    }

    public static Modulo paraDominio(ModuloEntity jpa) {
        List<UUID> atividadeIds = jpa.getAtividades().stream().map(AtividadeEntity::getId).toList();
        return new Modulo(jpa.getId(), jpa.getNome(), jpa.getCurso().getId(), atividadeIds);
    }

    public static Atividade paraDominio(AtividadeEntity jpa) {
        List<Questao> questoes = jpa.getQuestoes().stream().map(ConteudoMapper::paraDominio).toList();
        return new Atividade(jpa.getId(), jpa.getTitulo(), jpa.getModulo().getId(), questoes);
    }

    public static Questao paraDominio(QuestaoEntity jpa) {
        List<Alternativa> alternativas = jpa.getAlternativas().stream()
                .map(a -> new Alternativa(a.getId(), a.getDescricao(), a.isCorreta()))
                .toList();
        return new Questao(jpa.getId(), jpa.getEnunciado(), jpa.getValor(), alternativas);
    }

    public static void copiarQuestoes(Atividade dominio, AtividadeEntity jpa) {
        jpa.getQuestoes().clear();
        for (Questao questao : dominio.questoes()) {
            QuestaoEntity questaoJpa = new QuestaoEntity();
            questaoJpa.setEnunciado(questao.enunciado());
            questaoJpa.setValor(questao.valor());
            questaoJpa.setAtividade(jpa);
            for (Alternativa alternativa : questao.alternativas()) {
                AlternativaEntity alternativaJpa = new AlternativaEntity();
                alternativaJpa.setDescricao(alternativa.descricao());
                alternativaJpa.setCorreta(alternativa.correta());
                alternativaJpa.setQuestao(questaoJpa);
                questaoJpa.getAlternativas().add(alternativaJpa);
            }
            jpa.getQuestoes().add(questaoJpa);
        }
        jpa.setQuantQuestoes(dominio.quantQuestoes());
    }
}
