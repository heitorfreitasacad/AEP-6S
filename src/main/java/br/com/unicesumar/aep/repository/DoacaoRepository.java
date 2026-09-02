package br.com.unicesumar.aep.repository;

import br.com.unicesumar.aep.model.Doacao;
import br.com.unicesumar.aep.model.StatusDoacao;

import java.util.List;
import java.util.Optional;

public interface DoacaoRepository {

    Doacao salvar(Doacao doacao);

    Optional<Doacao> buscarPorId(String id);

    List<Doacao> listarTodas();

    boolean atualizarStatus(String id, StatusDoacao status);

    boolean remover(String id);
}
