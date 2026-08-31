package br.com.unicesumar.aep.exception;

public class DoacaoNaoEncontradaException extends RuntimeException {

    public DoacaoNaoEncontradaException(String id) {
        super("Doacao nao encontrada para o id: " + id);
    }
}
