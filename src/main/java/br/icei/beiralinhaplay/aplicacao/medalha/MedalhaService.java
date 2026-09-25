package br.icei.beiralinhaplay.aplicacao.medalha;

import br.icei.beiralinhaplay.dominio.compartilhado.ForbiddenException;
import br.icei.beiralinhaplay.dominio.compartilhado.ResourceNotFoundException;
import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;
import br.icei.beiralinhaplay.dominio.medalha.Medalha;
import br.icei.beiralinhaplay.dominio.medalha.MedalhaRepository;
import br.icei.beiralinhaplay.dominio.usuario.Aluno;
import br.icei.beiralinhaplay.dominio.usuario.UsuarioRepository;
import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MedalhaService {

    static final long TAMANHO_MAXIMO_BYTES = 2 * 1024 * 1024;
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp",
            "image/gif"
    );

    private final MedalhaRepository repositorioMedalha;
    private final UsuarioRepository repositorioUsuario;
    private final ArmazenamentoImagem armazenamentoImagem;

    public MedalhaService(
            MedalhaRepository repositorioMedalha,
            UsuarioRepository repositorioUsuario,
            ArmazenamentoImagem armazenamentoImagem
    ) {
        this.repositorioMedalha = repositorioMedalha;
        this.repositorioUsuario = repositorioUsuario;
        this.armazenamentoImagem = armazenamentoImagem;
    }

    public List<MedalhaComStatus> listar(Usuario solicitante) {
        Integer pontos = solicitante instanceof Aluno aluno ? aluno.pontos() : null;
        return repositorioMedalha.listar().stream()
                .map(medalha -> new MedalhaComStatus(medalha, pontos != null && pontos >= medalha.pontosMin()))
                .sorted(Comparator.comparingInt(item -> item.medalha().pontosMin()))
                .toList();
    }

    public Medalha criar(Usuario solicitante, SalvarMedalhaCommand comando) {
        exigirAdmin(solicitante);
        validarArquivo(comando);
        String imagemUrl = armazenamentoImagem.enviar(
                comando.imagem(),
                comando.contentType(),
                comando.nomeArquivo()
        );
        try {
            return repositorioMedalha.salvar(
                    new Medalha(null, comando.nome(), imagemUrl, comando.pontosMin())
            );
        } catch (RuntimeException ex) {
            armazenamentoImagem.excluir(imagemUrl);
            throw ex;
        }
    }

    public void excluir(Usuario solicitante, UUID id) {
        exigirAdmin(solicitante);
        Medalha medalha = repositorioMedalha.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medalha não encontrada"));
        try {
            armazenamentoImagem.excluir(medalha.imagemUrl());
        } catch (RuntimeException ignored) {
            // Catálogo some mesmo se o Cloudinary falhar; evita medalha órfã no banco.
        }
        repositorioMedalha.excluir(id);
    }

    public Aluno equiparAvatar(Usuario solicitante, UUID medalhaId) {
        if (!(solicitante instanceof Aluno aluno)) {
            throw new ForbiddenException("Apenas alunos equipam medalha como foto de pergil");
        }
        Medalha medalha = repositorioMedalha.buscarPorId(medalhaId)
                .orElseThrow(() -> new ResourceNotFoundException("Medalha não encontrada"));
        if (!aluno.conquistou(medalha)) {
            throw new BusinessRuleException("Você ainda não conquistou esta medalha");
        }
        aluno.definirImagemPerfil(medalha.imagemUrl());
        return (Aluno) repositorioUsuario.salvar(aluno);
    }

    private static void validarArquivo(SalvarMedalhaCommand comando) {
        if (comando.imagem() == null || comando.imagem().length == 0) {
            throw new BusinessRuleException("Informe a imagem da medalha");
        }
        if (comando.imagem().length > TAMANHO_MAXIMO_BYTES) {
            throw new BusinessRuleException("A imagem deve ter no máximo 2 MB");
        }
        String tipo = comando.contentType() == null ? "" : comando.contentType().toLowerCase();
        if (!TIPOS_PERMITIDOS.contains(tipo)) {
            throw new BusinessRuleException("Envie PNG, JPEG, WebP ou GIF");
        }
    }

    private static void exigirAdmin(Usuario solicitante) {
        if (solicitante.tipo() != TipoUsuario.ADMIN) {
            throw new ForbiddenException("Apenas o admin gerencia o catálogo de medalhas");
        }
    }
}
