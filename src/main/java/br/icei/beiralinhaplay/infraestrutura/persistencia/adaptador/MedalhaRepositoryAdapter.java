package br.icei.beiralinhaplay.infraestrutura.persistencia.adaptador;

import br.icei.beiralinhaplay.dominio.medalha.Medalha;
import br.icei.beiralinhaplay.dominio.medalha.MedalhaRepository;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.MedalhaEntity;
import br.icei.beiralinhaplay.infraestrutura.persistencia.jpa.MedalhaJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public class MedalhaRepositoryAdapter implements MedalhaRepository {

    private final MedalhaJpaRepository medalhaJpaRepository;

    public MedalhaRepositoryAdapter(MedalhaJpaRepository medalhaJpaRepository) {
        this.medalhaJpaRepository = medalhaJpaRepository;
    }

    @Override
    public Medalha salvar(Medalha medalha) {
        MedalhaEntity jpa = medalha.id() == null
                ? new MedalhaEntity()
                : medalhaJpaRepository.findById(medalha.id()).orElse(new MedalhaEntity());
        jpa.setNome(medalha.nome());
        jpa.setImagemUrl(medalha.imagemUrl());
        jpa.setPontosMin(medalha.pontosMin());
        MedalhaEntity salvo = medalhaJpaRepository.save(jpa);
        return paraDominio(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Medalha> buscarPorId(UUID id) {
        return medalhaJpaRepository.findById(id).map(MedalhaRepositoryAdapter::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Medalha> listar() {
        return medalhaJpaRepository.findAll().stream().map(MedalhaRepositoryAdapter::paraDominio).toList();
    }

    @Override
    public void excluir(UUID id) {
        medalhaJpaRepository.deleteById(id);
    }

    private static Medalha paraDominio(MedalhaEntity jpa) {
        return new Medalha(jpa.getId(), jpa.getNome(), jpa.getImagemUrl(), jpa.getPontosMin());
    }
}
