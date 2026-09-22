package br.icei.beiralinhaplay.aplicacao.questoes;

import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;
import br.icei.beiralinhaplay.dominio.compartilhado.ForbiddenException;
import br.icei.beiralinhaplay.dominio.compartilhado.ResourceNotFoundException;
import br.icei.beiralinhaplay.dominio.curso.Curso;
import br.icei.beiralinhaplay.dominio.curso.CursoRepository;
import br.icei.beiralinhaplay.dominio.modulo.Modulo;
import br.icei.beiralinhaplay.dominio.modulo.ModuloRepository;
import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GeracaoQuestoesService {

    static final String INSTRUCAO_SISTEMA = """
            Você gera questões de múltipla escolha para o Beira Linha Play, em português.

            Responda SOMENTE um JSON válido, sem markdown e sem texto extra, neste formato:
            {
              "questoes": [
                {
                  "enunciado": "enunciado da pergunta",
                  "valor": 1,
                  "alternativas": [
                    { "descricao": "alternativa A", "correta": false },
                    { "descricao": "alternativa B", "correta": true }
                  ]
                }
              ]
            }

            Regras:
            - 2 ou 4 alternativas por questão
            - exatamente uma alternativa correta (correta: true)
            - valor entre 1 e 3 (1 fácil, 2 médio, 3 difícil)
            - no máximo 10 questões
            - se o usuário pedir N perguntas, gere exatamente N
            - se o usuário não pedir quantidade, gere 3
            """;

    private static final int MIN_QUESTOES = 1;
    private static final int MAX_QUESTOES = 10;
    private static final String JSON_INVALIDO = "Não foi possível organizar as perguntas no formato da atividade";

    private final ModuloRepository repositorioModulo;
    private final CursoRepository repositorioCurso;
    private final GeradorQuestoes gerador;
    private final ObjectMapper mapper;

    public GeracaoQuestoesService(
            ModuloRepository repositorioModulo,
            CursoRepository repositorioCurso,
            GeradorQuestoes gerador
    ) {
        this.repositorioModulo = repositorioModulo;
        this.repositorioCurso = repositorioCurso;
        this.gerador = gerador;
        this.mapper = new ObjectMapper();
    }

    public List<QuestaoGerada> gerar(Usuario solicitante, UUID moduloId, GerarQuestoesCommand comando) {
        Modulo modulo = repositorioModulo.buscarPorId(moduloId)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo não encontrado"));
        Curso curso = repositorioCurso.buscarPorId(modulo.cursoId())
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado"));
        exigirMonitorDoCurso(solicitante, curso);

        String prompt = montarPrompt(comando);
        String bruto = gerador.gerar(prompt, INSTRUCAO_SISTEMA);
        return validar(parsear(bruto));
    }

    String montarPrompt(GerarQuestoesCommand comando) {
        String mensagem = comando.mensagem() == null ? "" : comando.mensagem().trim();
        if (comando.quantidadeQuestoes() == null) {
            return mensagem;
        }
        return mensagem
                + "\n\n(A atividade atual tem "
                + comando.quantidadeQuestoes()
                + " pergunta(s). Se o usuário não pedir outra quantidade, gere exatamente "
                + comando.quantidadeQuestoes()
                + ".)";
    }

    private List<QuestaoGerada> parsear(String bruto) {
        if (bruto == null || bruto.isBlank()) {
            throw new BusinessRuleException(JSON_INVALIDO);
        }
        try {
            JsonNode raiz = mapper.readTree(extrairJson(bruto));
            JsonNode lista = raiz.isArray() ? raiz : raiz.get("questoes");
            if (lista == null || !lista.isArray()) {
                throw new BusinessRuleException(JSON_INVALIDO);
            }
            List<QuestaoGerada> questoes = new ArrayList<>();
            for (JsonNode item : lista) {
                questoes.add(mapper.treeToValue(item, QuestaoGerada.class));
            }
            return questoes;
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            throw new BusinessRuleException(JSON_INVALIDO);
        }
    }

    private static List<QuestaoGerada> validar(List<QuestaoGerada> questoes) {
        if (questoes == null || questoes.size() < MIN_QUESTOES || questoes.size() > MAX_QUESTOES) {
            throw new BusinessRuleException(JSON_INVALIDO);
        }
        for (QuestaoGerada questao : questoes) {
            if (questao == null
                    || questao.enunciado() == null
                    || questao.enunciado().isBlank()
                    || questao.valor() < 1
                    || questao.valor() > 3
                    || !alternativasValidas(questao.alternativas())) {
                throw new BusinessRuleException(JSON_INVALIDO);
            }
        }
        return questoes;
    }

    private static boolean alternativasValidas(List<QuestaoGerada.AlternativaGerada> alternativas) {
        if (alternativas == null || (alternativas.size() != 2 && alternativas.size() != 4)) {
            return false;
        }
        long corretas = 0;
        for (QuestaoGerada.AlternativaGerada alternativa : alternativas) {
            if (alternativa == null
                    || alternativa.descricao() == null
                    || alternativa.descricao().isBlank()) {
                return false;
            }
            if (alternativa.correta()) {
                corretas++;
            }
        }
        return corretas == 1;
    }

    private static String extrairJson(String texto) {
        String trimmed = texto.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        int depoisDaCerca = trimmed.indexOf('\n');
        int fecha = trimmed.lastIndexOf("```");
        if (depoisDaCerca < 0 || fecha <= depoisDaCerca) {
            return trimmed;
        }
        return trimmed.substring(depoisDaCerca + 1, fecha).trim();
    }

    private static void exigirMonitorDoCurso(Usuario solicitante, Curso curso) {
        if (solicitante.tipo() != TipoUsuario.MONITOR || !curso.monitorIds().contains(solicitante.id())) {
            throw new ForbiddenException("Apenas o monitor do curso pode gerar questões");
        }
    }
}
