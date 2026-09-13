package br.icei.beiralinhaplay.dominio.usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {

    Usuario salvar(Usuario usuario);

    Optional<Usuario> buscarPorId(UUID id);

    Optional<Aluno> buscarAlunoPorId(UUID id);

    Optional<Monitor> buscarMonitorPorId(UUID id);

    Optional<Aluno> buscarAlunoPorApelido(String apelido);

    Optional<Monitor> buscarMonitorPorEmail(String email);

    Optional<Admin> buscarAdminPorNome(String nome);

    boolean existeApelido(String apelido, UUID ignorarId);

    boolean existeEmail(String email, UUID ignorarId);

    boolean existeNomeAdmin(String nome, UUID ignorarId);

    List<Monitor> listarMonitores();

    List<Aluno> listarAlunos();

    List<Aluno> listarAlunosDoCurso(UUID cursoId);
}
