package br.icei.beiralinhaplay.aplicacao.usuario;

import br.icei.beiralinhaplay.dominio.autenticacao.PasswordHasher;
import br.icei.beiralinhaplay.dominio.compartilhado.ForbiddenException;
import br.icei.beiralinhaplay.dominio.compartilhado.ResourceNotFoundException;
import br.icei.beiralinhaplay.dominio.compartilhado.BusinessRuleException;
import br.icei.beiralinhaplay.dominio.compartilhado.DomainRules;
import br.icei.beiralinhaplay.dominio.usuario.Admin;
import br.icei.beiralinhaplay.dominio.usuario.Aluno;
import br.icei.beiralinhaplay.dominio.usuario.Monitor;
import br.icei.beiralinhaplay.dominio.usuario.UsuarioRepository;
import br.icei.beiralinhaplay.dominio.usuario.TipoUsuario;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;

import java.util.List;
import java.util.UUID;

public class ContaService {

    private final UsuarioRepository repositorioUsuario;
    private final PasswordHasher codificadorSenha;

    public ContaService(
            UsuarioRepository repositorioUsuario,
            PasswordHasher codificadorSenha
    ) {
        this.repositorioUsuario = repositorioUsuario;
        this.codificadorSenha = codificadorSenha;
    }

    public Usuario atualizar(UUID usuarioId, AtualizarContaCommand comando) {
        Usuario usuario = repositorioUsuario.buscarPorId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        usuario.definirNome(comando.nome());

        if (usuario instanceof Aluno aluno) {
            if (comando.apelido() == null || comando.apelido().isBlank()) {
                throw new BusinessRuleException("Informe o apelido");
            }
            if (repositorioUsuario.existeApelido(comando.apelido().trim(), usuarioId)) {
                throw new BusinessRuleException("Apelido já em uso");
            }
            aluno.definirApelido(comando.apelido());
        }

        if (usuario instanceof Monitor) {
            if (comando.email() == null || comando.email().isBlank()) {
                throw new BusinessRuleException("Informe um e-mail válido");
            }
            if (repositorioUsuario.existeEmail(comando.email(), usuarioId)) {
                throw new BusinessRuleException("E-mail já em uso");
            }
            usuario.definirEmail(comando.email());
        }

        if (comando.senha() != null && !comando.senha().isBlank()) {
            if (comando.senha().length() < DomainRules.SENHA_MINIMA) {
                throw new BusinessRuleException("A senha deve ter pelo menos 6 caracteres");
            }
            usuario.definirSenhaHash(codificadorSenha.codificar(comando.senha()));
        }

        return repositorioUsuario.salvar(usuario);
    }

    public Admin criarAdmin(Usuario solicitante, CriarAdminCommand comando) {
        if (solicitante.tipo() != TipoUsuario.ADMIN) {
            throw new ForbiddenException("Apenas administradores podem criar outro admin");
        }
        if (comando.senha() == null || comando.senha().length() < DomainRules.SENHA_MINIMA) {
            throw new BusinessRuleException("A senha deve ter pelo menos 6 caracteres");
        }
        if (repositorioUsuario.existeNomeAdmin(comando.nome(), null)) {
            throw new BusinessRuleException("Já existe um admin com este nome");
        }
        Admin admin = new Admin(null, comando.nome(), null, codificadorSenha.codificar(comando.senha()));
        return (Admin) repositorioUsuario.salvar(admin);
    }

    public List<Monitor> listarMonitores(Usuario solicitante) {
        if (solicitante == null) {
            throw new ForbiddenException();
        }
        return repositorioUsuario.listarMonitores();
    }
}
