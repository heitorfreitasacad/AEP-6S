package br.com.unicesumar.aep.repository;

import br.com.unicesumar.aep.model.Instituicao;

import java.util.List;
import java.util.Optional;

public interface InstituicaoRepository {

    Instituicao salvar(Instituicao instituicao);

    Optional<Instituicao> buscarPorId(String id);

    List<Instituicao> listarTodas();

    boolean atualizar(Instituicao instituicao);

    boolean remover(String id);
}
