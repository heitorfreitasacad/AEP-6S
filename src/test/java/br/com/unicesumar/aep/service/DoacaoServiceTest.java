package br.com.unicesumar.aep.service;

import br.com.unicesumar.aep.exception.DoacaoInvalidaException;
import br.com.unicesumar.aep.exception.DoacaoNaoEncontradaException;
import br.com.unicesumar.aep.model.Doacao;
import br.com.unicesumar.aep.model.StatusDoacao;
import br.com.unicesumar.aep.repository.DoacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoacaoServiceTest {

    @Mock
    private DoacaoRepository repository;

    private DoacaoService service;

    @BeforeEach
    void setUp() {
        service = new DoacaoService(repository);
    }

    @Test
    void deveRegistrarDoacaoValida() {
        when(repository.salvar(any(Doacao.class))).thenAnswer(invocacao -> {
            Doacao doacao = invocacao.getArgument(0);
            doacao.setId("1");
            return doacao;
        });

        Doacao doacao = service.registrar("Maria", "Arroz", 5, "kg", LocalDate.now());

        assertEquals("1", doacao.getId());
        assertEquals(StatusDoacao.PENDENTE, doacao.getStatus());
        verify(repository).salvar(any(Doacao.class));
    }

    @Test
    void naoDeveRegistrarComDoadorVazio() {
        assertThrows(DoacaoInvalidaException.class,
                () -> service.registrar(" ", "Arroz", 5, "kg", LocalDate.now()));
        verifyNoInteractions(repository);
    }

    @Test
    void naoDeveRegistrarComItemVazio() {
        assertThrows(DoacaoInvalidaException.class,
                () -> service.registrar("Maria", "", 5, "kg", LocalDate.now()));
    }

    @Test
    void naoDeveRegistrarComQuantidadeInvalida() {
        assertThrows(DoacaoInvalidaException.class,
                () -> service.registrar("Maria", "Arroz", 0, "kg", LocalDate.now()));
    }

    @Test
    void naoDeveRegistrarComUnidadeVazia() {
        assertThrows(DoacaoInvalidaException.class,
                () -> service.registrar("Maria", "Arroz", 5, "", LocalDate.now()));
    }

    @Test
    void naoDeveRegistrarComDataNula() {
        assertThrows(DoacaoInvalidaException.class,
                () -> service.registrar("Maria", "Arroz", 5, "kg", null));
    }

    @Test
    void naoDeveRegistrarComDataFutura() {
        assertThrows(DoacaoInvalidaException.class,
                () -> service.registrar("Maria", "Arroz", 5, "kg", LocalDate.now().plusDays(1)));
    }

    @Test
    void deveListarTodasAsDoacoes() {
        Doacao doacao = new Doacao("Maria", "Arroz", 5, "kg", LocalDate.now());
        when(repository.listarTodas()).thenReturn(List.of(doacao));

        List<Doacao> lista = service.listar();

        assertEquals(1, lista.size());
        verify(repository).listarTodas();
    }

    @Test
    void deveBuscarPorIdExistente() {
        Doacao doacao = new Doacao("Maria", "Arroz", 5, "kg", LocalDate.now());
        doacao.setId("1");
        when(repository.buscarPorId("1")).thenReturn(Optional.of(doacao));

        Doacao encontrada = service.buscarPorId("1");

        assertEquals("1", encontrada.getId());
    }

    @Test
    void deveLancarExcecaoAoBuscarIdInexistente() {
        when(repository.buscarPorId("99")).thenReturn(Optional.empty());

        assertThrows(DoacaoNaoEncontradaException.class, () -> service.buscarPorId("99"));
    }

    @Test
    void deveAtualizarStatusComSucesso() {
        when(repository.atualizarStatus("1", StatusDoacao.RECEBIDA)).thenReturn(true);

        assertDoesNotThrow(() -> service.atualizarStatus("1", StatusDoacao.RECEBIDA));
    }

    @Test
    void deveLancarExcecaoAoAtualizarStatusInexistente() {
        when(repository.atualizarStatus("99", StatusDoacao.RECEBIDA)).thenReturn(false);

        assertThrows(DoacaoNaoEncontradaException.class,
                () -> service.atualizarStatus("99", StatusDoacao.RECEBIDA));
    }

    @Test
    void naoDeveAtualizarComStatusNulo() {
        assertThrows(DoacaoInvalidaException.class, () -> service.atualizarStatus("1", null));
        verifyNoInteractions(repository);
    }

    @Test
    void deveRemoverComSucesso() {
        when(repository.remover("1")).thenReturn(true);

        assertDoesNotThrow(() -> service.remover("1"));
    }

    @Test
    void deveLancarExcecaoAoRemoverInexistente() {
        when(repository.remover("99")).thenReturn(false);

        assertThrows(DoacaoNaoEncontradaException.class, () -> service.remover("99"));
    }
}
