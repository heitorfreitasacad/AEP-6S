package br.com.unicesumar.aep.exception;

public class InstituicaoNaoEncontradaException extends RuntimeException {

    public InstituicaoNaoEncontradaException(String id) {
        super("Instituicao nao encontrada para o id: " + id);
    }
}
