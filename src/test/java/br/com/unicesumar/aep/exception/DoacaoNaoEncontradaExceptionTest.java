package br.com.unicesumar.aep.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DoacaoNaoEncontradaExceptionTest {

    @Test
    void deveMontarMensagemComId() {
        DoacaoNaoEncontradaException excecao = new DoacaoNaoEncontradaException("123");

        assertEquals("Doacao nao encontrada para o id: 123", excecao.getMessage());
    }
}
