package br.icei.beiralinhaplay.infraestrutura.configuracao;

import br.icei.beiralinhaplay.aplicacao.atividade.AtividadeService;
import br.icei.beiralinhaplay.aplicacao.autenticacao.AutenticacaoService;
import br.icei.beiralinhaplay.aplicacao.curso.CursoService;
import br.icei.beiralinhaplay.aplicacao.questoes.GeracaoQuestoesService;
import br.icei.beiralinhaplay.aplicacao.questoes.GeradorQuestoes;
import br.icei.beiralinhaplay.aplicacao.medalha.ArmazenamentoImagem;
import br.icei.beiralinhaplay.aplicacao.medalha.MedalhaService;
import br.icei.beiralinhaplay.aplicacao.modulo.ModuloService;
import br.icei.beiralinhaplay.aplicacao.ranking.RankingService;
import br.icei.beiralinhaplay.aplicacao.tentativa.TentativaService;
import br.icei.beiralinhaplay.aplicacao.usuario.ContaService;
import br.icei.beiralinhaplay.aplicacao.usuario.ImportarInscritosService;
import br.icei.beiralinhaplay.dominio.atividade.AtividadeRepository;
import br.icei.beiralinhaplay.dominio.autenticacao.PasswordHasher;
import br.icei.beiralinhaplay.dominio.autenticacao.RefreshTokenGenerator;
import br.icei.beiralinhaplay.dominio.autenticacao.AccessTokenProvider;
import br.icei.beiralinhaplay.dominio.autenticacao.TokenAtualizacaoRepository;
import br.icei.beiralinhaplay.dominio.curso.CursoRepository;
import br.icei.beiralinhaplay.dominio.medalha.MedalhaRepository;
import br.icei.beiralinhaplay.dominio.modulo.ModuloRepository;
import br.icei.beiralinhaplay.dominio.tentativa.TentativaRepository;
import br.icei.beiralinhaplay.dominio.usuario.UsuarioRepository;
import br.icei.beiralinhaplay.dominio.importacao.LogImportacaoRepository;
import br.icei.beiralinhaplay.infraestrutura.integracoes.sympla.ImportadorParticipantesSympla;
import br.icei.beiralinhaplay.infraestrutura.integracoes.sympla.CodificadorEventoSympla;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(ApplicationProperties.class)
public class UseCaseConfig {

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    AutenticacaoService servicoAutenticacao(
            UsuarioRepository repositorioUsuario,
            TokenAtualizacaoRepository repositorioTokenAtualizacao,
            PasswordHasher codificadorSenha,
            AccessTokenProvider provedorTokenAcesso,
            RefreshTokenGenerator geradorTokenAtualizacao,
            ApplicationProperties propriedades,
            Clock relogio
    ) {
        return new AutenticacaoService(
                repositorioUsuario,
                repositorioTokenAtualizacao,
                codificadorSenha,
                provedorTokenAcesso,
                geradorTokenAtualizacao,
                Duration.ofDays(propriedades.getJwt().getRefreshTokenDias()),
                relogio
        );
    }

    @Bean
    ContaService servicoConta(
            UsuarioRepository repositorioUsuario,
            PasswordHasher codificadorSenha
    ) {
        return new ContaService(repositorioUsuario, codificadorSenha);
    }

    @Bean
    CursoService servicoCurso(CursoRepository repositorioCurso, UsuarioRepository repositorioUsuario) {
        return new CursoService(repositorioCurso, repositorioUsuario);
    }

    @Bean
    ModuloService servicoModulo(
            ModuloRepository repositorioModulo,
            CursoRepository repositorioCurso,
            AtividadeRepository repositorioAtividade
    ) {
        return new ModuloService(repositorioModulo, repositorioCurso, repositorioAtividade);
    }

    @Bean
    GeracaoQuestoesService servicoGeracaoQuestoes(
            ModuloRepository repositorioModulo,
            CursoRepository repositorioCurso,
            GeradorQuestoes geradorQuestoes
    ) {
        return new GeracaoQuestoesService(repositorioModulo, repositorioCurso, geradorQuestoes);
    }

    @Bean
    ImportarInscritosService importarInscritosService(
            ImportadorParticipantesSympla importadorParticipantes,
            CodificadorEventoSympla codificadorEvento,
            UsuarioRepository repositorioUsuario,
            CursoRepository repositorioCurso,
            LogImportacaoRepository repositorioLog,
            PasswordHasher codificadorSenha
    ) {
        return new ImportarInscritosService(
                importadorParticipantes,
                codificadorEvento,
                repositorioUsuario,
                repositorioCurso,
                repositorioLog,
                codificadorSenha
        );
    }

    @Bean
    AtividadeService servicoAtividade(
            AtividadeRepository repositorioAtividade,
            ModuloRepository repositorioModulo,
            CursoRepository repositorioCurso
    ) {
        return new AtividadeService(repositorioAtividade, repositorioModulo, repositorioCurso);
    }

    @Bean
    TentativaService servicoTentativa(
            TentativaRepository repositorioTentativa,
            AtividadeRepository repositorioAtividade,
            ModuloRepository repositorioModulo,
            CursoRepository repositorioCurso,
            UsuarioRepository repositorioUsuario,
            Clock relogio
    ) {
        return new TentativaService(
                repositorioTentativa,
                repositorioAtividade,
                repositorioModulo,
                repositorioCurso,
                repositorioUsuario,
                relogio
        );
    }

    @Bean
    RankingService servicoRanking(UsuarioRepository repositorioUsuario, CursoRepository repositorioCurso) {
        return new RankingService(repositorioUsuario, repositorioCurso);
    }

    @Bean
    MedalhaService servicoMedalha(
            MedalhaRepository repositorioMedalha,
            UsuarioRepository repositorioUsuario,
            ArmazenamentoImagem armazenamentoImagem
    ) {
        return new MedalhaService(repositorioMedalha, repositorioUsuario, armazenamentoImagem);
    }
}
