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
import java.util.UUID;

public class MedalhaService {

    private final MedalhaRepository repositorioMedalha;
    private final UsuarioRepository repositorioUsuario;

    public MedalhaService(MedalhaRepository repositorioMedalha, UsuarioRepository repositorioUsuario) {
        this.repositorioMedalha = repositorioMedalha;
        this.repositorioUsuario = repositorioUsuario;
    }

    public List<MedalhaComStatus> listar(Usuario solicitante) {
        Integer pontos = solicitante instanceof Aluno aluno ? aluno.pontos() : null;
        return repositorioMedalha.listar().stream()
                .map(medalha -> new MedalhaComStatus(medalha, pontos != null && pontos >= medalha.pontosMin()))
                .sorted(new Comparator<MedalhaComStatus>() {
                    @Override
                    public int compare(MedalhaComStatus o1, MedalhaComStatus o2) {
                        return Integer.compare(o1.medalha().pontosMin(), o2.medalha().pontosMin());
                    }
                })
                .toList();
    }

    public Medalha criar(Usuario solicitante, SalvarMedalhaCommand comando) {
        exigirAdmin(solicitante);
        return repositorioMedalha.salvar(new Medalha(null, comando.nome(), comando.imagemUrl(), comando.pontosMin()));
    }

    public void excluir(Usuario solicitante, UUID id) {
        exigirAdmin(solicitante);
        repositorioMedalha.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medalha não encontrada"));
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

    private static void exigirAdmin(Usuario solicitante) {
        if (solicitante.tipo() != TipoUsuario.ADMIN) {
            throw new ForbiddenException("Apenas o admin gerencia o catálogo de medalhas");
        }
    }
}
