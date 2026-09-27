package br.icei.beiralinhaplay.infraestrutura.persistencia.adaptador;

import br.icei.beiralinhaplay.dominio.usuario.Admin;
import br.icei.beiralinhaplay.dominio.usuario.Aluno;
import br.icei.beiralinhaplay.dominio.usuario.Monitor;
import br.icei.beiralinhaplay.dominio.usuario.UsuarioRepository;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.AdminEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.repositorios.AdminJpaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.AlunoEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.repositorios.AlunoJpaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.CursoEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.repositorios.CursoJpaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.MonitorEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.repositorios.MonitorJpaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.entidades.UsuarioEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.repositorios.UsuarioJpaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.mapeamento.UsuarioMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@Transactional
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository usuarioJpaRepository;
    private final AlunoJpaRepository alunoJpaRepository;
    private final MonitorJpaRepository monitorJpaRepository;
    private final AdminJpaRepository adminJpaRepository;
    private final CursoJpaRepository cursoJpaRepository;

    public UsuarioRepositoryAdapter(
            UsuarioJpaRepository usuarioJpaRepository,
            AlunoJpaRepository alunoJpaRepository,
            MonitorJpaRepository monitorJpaRepository,
            AdminJpaRepository adminJpaRepository,
            CursoJpaRepository cursoJpaRepository
    ) {
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.alunoJpaRepository = alunoJpaRepository;
        this.monitorJpaRepository = monitorJpaRepository;
        this.adminJpaRepository = adminJpaRepository;
        this.cursoJpaRepository = cursoJpaRepository;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        return switch (usuario) {
            case Aluno aluno -> UsuarioMapper.paraDominio(salvarAluno(aluno));
            case Monitor monitor -> UsuarioMapper.paraDominio(salvarMonitor(monitor));
            case Admin admin -> UsuarioMapper.paraDominio(salvarAdmin(admin));
            default -> throw new IllegalStateException("Tipo de usuário não suportado");
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(UUID id) {
        return usuarioJpaRepository.findById(id).flatMap(jpa -> {
            if (jpa instanceof AlunoEntity) {
                return alunoJpaRepository.buscarCompleto(id).map(UsuarioMapper::paraDominio);
            }
            if (jpa instanceof MonitorEntity) {
                return monitorJpaRepository.buscarCompleto(id).map(UsuarioMapper::paraDominio);
            }
            return Optional.of(UsuarioMapper.paraDominio(jpa));
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarNaoExpiradoPorId(UUID id, LocalDate hoje) {
        return usuarioJpaRepository.buscarNaoExpirado(id, hoje)
                .map(UsuarioEntity::getId)
                .flatMap(this::buscarPorId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Aluno> buscarAlunoPorId(UUID id) {
        return alunoJpaRepository.buscarCompleto(id).map(UsuarioMapper::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Monitor> buscarMonitorPorId(UUID id) {
        return monitorJpaRepository.buscarCompleto(id).map(UsuarioMapper::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Aluno> buscarAlunoPorApelido(String apelido) {
        if (apelido == null) {
            return Optional.empty();
        }
        return alunoJpaRepository.findByApelidoIgnoreCase(apelido.trim()).map(UsuarioMapper::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Monitor> buscarMonitorPorEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return monitorJpaRepository.findByEmailIgnoreCase(email.trim()).map(UsuarioMapper::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Admin> buscarAdminPorNome(String nome) {
        if (nome == null) {
            return Optional.empty();
        }
        return adminJpaRepository.findByNomeIgnoreCase(nome.trim()).map(UsuarioMapper::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeApelido(String apelido, UUID ignorarId) {
        return alunoJpaRepository.existsApelido(apelido, ignorarId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEmail(String email, UUID ignorarId) {
        if (email == null) {
            return false;
        }
        return monitorJpaRepository.existsEmail(email, ignorarId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeNomeAdmin(String nome, UUID ignorarId) {
        return adminJpaRepository.existsNome(nome, ignorarId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Monitor> listarMonitores() {
        return monitorJpaRepository.listarTodos().stream().map(UsuarioMapper::paraDominio).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Aluno> listarAlunos() {
        return alunoJpaRepository.listarTodos().stream().map(UsuarioMapper::paraDominio).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Aluno> listarAlunosDoCurso(UUID cursoId) {
        return alunoJpaRepository.findByCursoId(cursoId).stream().map(UsuarioMapper::paraDominio).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        List<Usuario> encontrados = listarPorEmail(email);
        if (encontrados.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(encontrados.getFirst());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarPorEmail(String email) {
        if (email == null || email.isBlank()) {
            return List.of();
        }
        return usuarioJpaRepository.findAllByEmailIgnoreCase(email.trim()).stream()
                .map(jpa -> buscarPorId(jpa.getId()))
                .flatMap(Optional::stream)
                .toList();
    }

    private AlunoEntity salvarAluno(Aluno aluno) {
        AlunoEntity jpa = aluno.id() == null
                ? new AlunoEntity()
                : alunoJpaRepository.buscarCompleto(aluno.id()).orElse(new AlunoEntity());
        UsuarioMapper.copiarBase(aluno, jpa);
        jpa.setApelido(aluno.apelido());
        jpa.setPontos(aluno.pontos());
        jpa.setImagemPerfil(aluno.imagemPerfil());
        jpa.setLogImportacaoId(aluno.logImportacaoId());
        jpa.setCursos(cursos(aluno.cursoIds()));
        return alunoJpaRepository.save(jpa);
    }

    private MonitorEntity salvarMonitor(Monitor monitor) {
        MonitorEntity jpa = monitor.id() == null
                ? new MonitorEntity()
                : monitorJpaRepository.buscarCompleto(monitor.id()).orElse(new MonitorEntity());
        UsuarioMapper.copiarBase(monitor, jpa);
        jpa.setCursoOrigem(monitor.cursoOrigem());
        jpa.setCursos(cursos(monitor.cursoIds()));
        return monitorJpaRepository.save(jpa);
    }

    private AdminEntity salvarAdmin(Admin admin) {
        AdminEntity jpa = admin.id() == null
                ? new AdminEntity()
                : adminJpaRepository.findById(admin.id()).orElse(new AdminEntity());
        UsuarioMapper.copiarBase(admin, jpa);
        return adminJpaRepository.save(jpa);
    }

    private Set<CursoEntity> cursos(List<UUID> ids) {
        return new HashSet<>(cursoJpaRepository.findAllById(ids));
    }
}
