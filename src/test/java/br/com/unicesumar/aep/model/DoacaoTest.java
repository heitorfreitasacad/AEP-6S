package br.com.unicesumar.aep.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DoacaoTest {

    @Test
    void deveCriarComConstrutorPreenchidoEStatusPendente() {
        Doacao doacao = new Doacao("Joao", "Feijao", 10, "kg", LocalDate.of(2026, 1, 1));

        assertEquals("Joao", doacao.getDoador());
        assertEquals("Feijao", doacao.getItem());
        assertEquals(10, doacao.getQuantidade());
        assertEquals("kg", doacao.getUnidade());
        assertEquals(LocalDate.of(2026, 1, 1), doacao.getDataDoacao());
        assertEquals(StatusDoacao.PENDENTE, doacao.getStatus());
    }

    @Test
    void deveAlterarCamposComSetters() {
        Doacao doacao = new Doacao();
        doacao.setId("1");
        doacao.setDoador("Maria");
        doacao.setItem("Arroz");
        doacao.setQuantidade(5);
        doacao.setUnidade("kg");
        doacao.setDataDoacao(LocalDate.now());
        doacao.setStatus(StatusDoacao.RECEBIDA);

        assertEquals("1", doacao.getId());
        assertEquals("Maria", doacao.getDoador());
        assertEquals("Arroz", doacao.getItem());
        assertEquals(5, doacao.getQuantidade());
        assertEquals("kg", doacao.getUnidade());
        assertEquals(StatusDoacao.RECEBIDA, doacao.getStatus());
    }

    @Test
    void deveConsiderarIguaisPeloId() {
        Doacao d1 = new Doacao();
        d1.setId("1");
        Doacao d2 = new Doacao();
        d2.setId("1");
        Doacao d3 = new Doacao();
        d3.setId("2");

        assertEquals(d1, d2);
        assertEquals(d1, d1);
        assertNotEquals(d1, d3);
        assertNotEquals(d1, null);
        assertNotEquals(d1, "string");
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void toStringDeveConterCamposPrincipais() {
        Doacao doacao = new Doacao("Joao", "Feijao", 10, "kg", LocalDate.of(2026, 1, 1));
        doacao.setId("1");

        String texto = doacao.toString();

        assertTrue(texto.contains("Joao"));
        assertTrue(texto.contains("Feijao"));
    }
}
