package br.com.unicesumar.aep.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DoacaoInvalidaExceptionTest {

    @Test
    void devePreservarMensagem() {
        DoacaoInvalidaException excecao = new DoacaoInvalidaException("Quantidade deve ser maior que zero.");

        assertEquals("Quantidade deve ser maior que zero.", excecao.getMessage());
    }
}
