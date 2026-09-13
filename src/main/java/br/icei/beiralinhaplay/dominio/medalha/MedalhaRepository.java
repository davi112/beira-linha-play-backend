package br.icei.beiralinhaplay.dominio.medalha;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedalhaRepository {

    Medalha salvar(Medalha medalha);

    Optional<Medalha> buscarPorId(UUID id);

    List<Medalha> listar();

    void excluir(UUID id);
}
