package br.icei.beiralinhaplay.aplicacao;

import br.icei.beiralinhaplay.aplicacao.medalha.ArmazenamentoImagem;
import br.icei.beiralinhaplay.aplicacao.medalha.MedalhaService;
import br.icei.beiralinhaplay.aplicacao.medalha.SalvarMedalhaCommand;
import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;
import br.icei.beiralinhaplay.dominio.compartilhado.ForbiddenException;
import br.icei.beiralinhaplay.dominio.medalha.Medalha;
import br.icei.beiralinhaplay.dominio.medalha.MedalhaRepository;
import br.icei.beiralinhaplay.dominio.usuario.Admin;
import br.icei.beiralinhaplay.dominio.usuario.Aluno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MedalhaServiceTest {

    private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-4000-8000-0000000000e1");
    private static final UUID ALUNO_ID = UUID.fromString("00000000-0000-4000-8000-0000000000d1");

    private final FakeMedalhas medalhas = new FakeMedalhas();
    private final FakeArmazenamento armazenamento = new FakeArmazenamento();
    private MedalhaService servico;
    private Admin admin;
    private Aluno aluno;

    @BeforeEach
    void setup() {
        admin = new Admin(ADMIN_ID, "Admin", null, "hash");
        aluno = new Aluno(ALUNO_ID, "Gu", "gu@icei.br", "hash", List.of(), "Gu", 0, "");
        servico = new MedalhaService(medalhas, null, armazenamento);
    }

    @Test
    void adminCriaMedalhaEnviandoArquivo() {
        Medalha criada = servico.criar(admin, comandoPng("PUC", 10));

        assertEquals("PUC", criada.nome());
        assertEquals("https://res.cloudinary.com/demo/image/upload/medalha.png", criada.imagemUrl());
        assertEquals(1, armazenamento.envios);
        assertEquals(1, medalhas.porId.size());
    }

    @Test
    void alunoNaoCriaMedalha() {
        assertThrows(ForbiddenException.class, () -> servico.criar(aluno, comandoPng("PUC", 0)));
        assertEquals(0, armazenamento.envios);
    }

    @Test
    void rejeitaArquivoQueNaoEImagem() {
        SalvarMedalhaCommand comando = new SalvarMedalhaCommand(
                "PUC",
                0,
                new byte[]{1, 2, 3},
                "application/pdf",
                "doc.pdf"
        );
        assertThrows(BusinessRuleException.class, () -> servico.criar(admin, comando));
        assertEquals(0, armazenamento.envios);
    }

    @Test
    void excluiMedalhaEPedeRemocaoDaImagem() {
        Medalha medalha = servico.criar(admin, comandoPng("PUC", 0));

        servico.excluir(admin, medalha.id());

        assertTrue(medalhas.porId.isEmpty());
        assertEquals(1, armazenamento.exclusoes.size());
        assertEquals(medalha.imagemUrl(), armazenamento.exclusoes.getFirst());
    }

    @Test
    void apagaDoBancoMesmoSeCloudinaryFalhar() {
        Medalha medalha = servico.criar(admin, comandoPng("PUC", 0));
        armazenamento.falharExclusao = true;

        servico.excluir(admin, medalha.id());

        assertTrue(medalhas.porId.isEmpty());
    }

    private static SalvarMedalhaCommand comandoPng(String nome, int pontos) {
        return new SalvarMedalhaCommand(nome, pontos, new byte[]{1, 2, 3}, "image/png", "medalha.png");
    }

    static final class FakeMedalhas implements MedalhaRepository {
        final Map<UUID, Medalha> porId = new HashMap<>();

        @Override
        public Medalha salvar(Medalha medalha) {
            if (medalha.id() == null) {
                medalha.definirId(UUID.randomUUID());
            }
            porId.put(medalha.id(), medalha);
            return medalha;
        }

        @Override
        public Optional<Medalha> buscarPorId(UUID id) {
            return Optional.ofNullable(porId.get(id));
        }

        @Override
        public List<Medalha> listar() {
            return List.copyOf(porId.values());
        }

        @Override
        public void excluir(UUID id) {
            porId.remove(id);
        }
    }

    static final class FakeArmazenamento implements ArmazenamentoImagem {
        int envios;
        boolean falharExclusao;
        final List<String> exclusoes = new ArrayList<>();

        @Override
        public String enviar(byte[] conteudo, String contentType, String nomeArquivo) {
            envios++;
            return "https://res.cloudinary.com/demo/image/upload/medalha.png";
        }

        @Override
        public void excluir(String imagemUrl) {
            if (falharExclusao) {
                throw new RuntimeException("cloudinary");
            }
            exclusoes.add(imagemUrl);
        }
    }
}
