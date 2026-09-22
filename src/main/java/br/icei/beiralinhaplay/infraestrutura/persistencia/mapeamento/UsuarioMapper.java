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
        return new Aluno(
                jpa.getId(),
                jpa.getNome(),
                jpa.getEmail(),
                jpa.getSenha(),
                cursoIds,
                jpa.getApelido(),
                jpa.getPontos(),
                jpa.getImagemPerfil()
        );
    }

    public static Monitor paraDominio(MonitorEntity jpa) {
        List<UUID> cursoIds = jpa.getCursos().stream().map(CursoEntity::getId).toList();
        return new Monitor(
                jpa.getId(),
                jpa.getNome(),
                jpa.getEmail(),
                jpa.getSenha(),
                cursoIds,
                jpa.getCursoOrigem()
        );
    }

    public static Admin paraDominio(AdminEntity jpa) {
        return new Admin(jpa.getId(), jpa.getNome(), jpa.getEmail(), jpa.getSenha());
    }

    public static void copiarBase(Usuario dominio, UsuarioEntity jpa) {
        jpa.setNome(dominio.nome());
        jpa.setEmail(dominio.email());
        jpa.setSenha(dominio.senhaHash());
    }
}
