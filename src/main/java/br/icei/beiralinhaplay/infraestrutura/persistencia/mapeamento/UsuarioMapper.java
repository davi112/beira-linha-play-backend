package br.icei.beiralinhaplay.infraestrutura.persistencia.mapeamento;

import br.icei.beiralinhaplay.dominio.usuario.Admin;
import br.icei.beiralinhaplay.dominio.usuario.Aluno;
import br.icei.beiralinhaplay.dominio.usuario.Monitor;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.AdminEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.AlunoEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.CursoEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.MonitorEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.UsuarioEntity;

import java.util.List;
import java.util.UUID;

public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static Usuario paraDominio(UsuarioEntity jpa) {
        return switch (jpa) {
            case AlunoEntity aluno -> paraDominio(aluno);
            case MonitorEntity monitor -> paraDominio(monitor);
            case AdminEntity admin -> paraDominio(admin);
            default -> throw new IllegalStateException("Tipo de usuário não suportado");
        };
    }

    public static Aluno paraDominio(AlunoEntity jpa) {
        List<UUID> cursoIds = jpa.getCursos().stream().map(CursoEntity::getId).toList();
        Aluno aluno = new Aluno(
                jpa.getId(),
                jpa.getNome(),
                jpa.getEmail(),
                jpa.getSenha(),
                cursoIds,
                jpa.getApelido(),
                jpa.getPontos(),
                jpa.getImagemPerfil()
        );
        aluno.definirLogImportacao(jpa.getLogImportacaoId());
        copiarAcesso(jpa, aluno);
        marcarSenhaPendente(jpa, aluno);
        return aluno;
    }

    public static Monitor paraDominio(MonitorEntity jpa) {
        List<UUID> cursoIds = jpa.getCursos().stream().map(CursoEntity::getId).toList();
        Monitor monitor = new Monitor(
                jpa.getId(),
                jpa.getNome(),
                jpa.getEmail(),
                jpa.getSenha(),
                cursoIds,
                jpa.getCursoOrigem()
        );
        copiarAcesso(jpa, monitor);
        marcarSenhaPendente(jpa, monitor);
        return monitor;
    }

    public static Admin paraDominio(AdminEntity jpa) {
        Admin admin = new Admin(jpa.getId(), jpa.getNome(), jpa.getEmail(), jpa.getSenha());
        copiarAcesso(jpa, admin);
        marcarSenhaPendente(jpa, admin);
        return admin;
    }

    public static void copiarBase(Usuario dominio, UsuarioEntity jpa) {
        jpa.setNome(dominio.nome());
        jpa.setEmail(dominio.email());
        jpa.setSenha(dominio.senhaHash());
        jpa.setDeveDefinirSenha(dominio.deveDefinirSenha());
        jpa.setAcessoExpiraEm(dominio.acessoExpiraEm());
    }

    private static void copiarAcesso(UsuarioEntity jpa, Usuario dominio) {
        dominio.definirAcessoExpiraEm(jpa.getAcessoExpiraEm());
    }

    private static void marcarSenhaPendente(UsuarioEntity jpa, Usuario dominio) {
        if (jpa.isDeveDefinirSenha()) {
            dominio.exigirNovaSenha();
        }
    }
}
