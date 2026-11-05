package br.com.unicesumar.aep.service;

import br.com.unicesumar.aep.exception.InstituicaoInvalidaException;
import br.com.unicesumar.aep.exception.InstituicaoNaoEncontradaException;
import br.com.unicesumar.aep.model.Contato;
import br.com.unicesumar.aep.model.Endereco;
import br.com.unicesumar.aep.model.Instituicao;
import br.com.unicesumar.aep.repository.InstituicaoRepository;

import java.util.List;

/**
 * Regras de negocio do cadastro de instituicoes: validacao dos dados
 * (incluindo endereco e contatos aninhados) e traducao de "nao encontrado"
 * em excecao de dominio.
 */
public class InstituicaoService {

    private final InstituicaoRepository repository;

    public InstituicaoService(InstituicaoRepository repository) {
        this.repository = repository;
    }

    public Instituicao registrar(String nome, Endereco endereco, List<Contato> contatos) {
        validar(nome, endereco, contatos);
        return repository.salvar(new Instituicao(nome, endereco, contatos));
    }

    public List<Instituicao> listar() {
        return repository.listarTodas();
    }

    public Instituicao buscarPorId(String id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new InstituicaoNaoEncontradaException(id));
    }

    public Instituicao atualizar(String id, String nome, Endereco endereco, List<Contato> contatos) {
        validar(nome, endereco, contatos);
        Instituicao instituicao = new Instituicao(nome, endereco, contatos);
        instituicao.setId(id);
        if (!repository.atualizar(instituicao)) {
            throw new InstituicaoNaoEncontradaException(id);
        }
        return instituicao;
    }

    public void remover(String id) {
        if (!repository.remover(id)) {
            throw new InstituicaoNaoEncontradaException(id);
        }
    }

    private void validar(String nome, Endereco endereco, List<Contato> contatos) {
        if (nome == null || nome.isBlank()) {
            throw new InstituicaoInvalidaException("Nome e obrigatorio.");
        }
        if (endereco == null) {
            throw new InstituicaoInvalidaException("Endereco e obrigatorio.");
        }
        if (endereco.getRua() == null || endereco.getRua().isBlank()
                || endereco.getCidade() == null || endereco.getCidade().isBlank()
                || endereco.getEstado() == null || endereco.getEstado().isBlank()) {
            throw new InstituicaoInvalidaException("Endereco deve ter rua, cidade e estado.");
        }
        if (contatos != null) {
            for (Contato contato : contatos) {
                if (contato.getNome() == null || contato.getNome().isBlank()) {
                    throw new InstituicaoInvalidaException("Todo contato deve ter nome.");
                }
            }
        }
    }
}
