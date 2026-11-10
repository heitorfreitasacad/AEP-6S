package br.com.unicesumar.aep.service;

import br.com.unicesumar.aep.exception.DoacaoInvalidaException;
import br.com.unicesumar.aep.exception.DoacaoNaoEncontradaException;
import br.com.unicesumar.aep.exception.InstituicaoNaoEncontradaException;
import br.com.unicesumar.aep.model.Doacao;
import br.com.unicesumar.aep.model.Endereco;
import br.com.unicesumar.aep.model.Instituicao;
import br.com.unicesumar.aep.model.StatusDoacao;
import br.com.unicesumar.aep.repository.DoacaoRepository;
import br.com.unicesumar.aep.repository.InstituicaoRepository;
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

    private static final String ID_INST = "inst1";

    @Mock
    private DoacaoRepository repository;

    @Mock
    private InstituicaoRepository instituicaoRepository;

    private DoacaoService service;

    @BeforeEach
    void setUp() {
        service = new DoacaoService(repository, instituicaoRepository);
    }

    private Instituicao instituicaoExistente() {
        Instituicao instituicao = new Instituicao("Banco de Alimentos", new Endereco("Rua A", "Maringa", "PR"), null);
        instituicao.setId(ID_INST);
        return instituicao;
    }

    @Test
    void deveRegistrarDoacaoValida() {
        when(instituicaoRepository.buscarPorId(ID_INST)).thenReturn(Optional.of(instituicaoExistente()));
        when(repository.salvar(any(Doacao.class))).thenAnswer(invocacao -> {
            Doacao doacao = invocacao.getArgument(0);
            doacao.setId("1");
            return doacao;
        });

        Doacao doacao = service.registrar("Maria", "Arroz", 5, "kg", LocalDate.now(), ID_INST);

        assertEquals("1", doacao.getId());
        assertEquals(StatusDoacao.PENDENTE, doacao.getStatus());
        assertEquals(ID_INST, doacao.getInstituicaoId());
        verify(repository).salvar(any(Doacao.class));
    }

    @Test
    void naoDeveRegistrarComInstituicaoInexistente() {
        when(instituicaoRepository.buscarPorId("99")).thenReturn(Optional.empty());

        assertThrows(DoacaoInvalidaException.class,
                () -> service.registrar("Maria", "Arroz", 5, "kg", LocalDate.now(), "99"));
        verifyNoInteractions(repository);
    }

    @Test
    void naoDeveRegistrarSemInstituicao() {
        assertThrows(DoacaoInvalidaException.class,
                () -> service.registrar("Maria", "Arroz", 5, "kg", LocalDate.now(), null));
        assertThrows(DoacaoInvalidaException.class,
                () -> service.registrar("Maria", "Arroz", 5, "kg", LocalDate.now(), " "));
        verifyNoInteractions(repository, instituicaoRepository);
    }

    @Test
    void naoDeveRegistrarComDataFutura() {
        assertThrows(DoacaoInvalidaException.class,
                () -> service.registrar("Maria", "Arroz", 5, "kg", LocalDate.now().plusDays(1), ID_INST));
    }

    @Test
    void deveListarDoacoesDeUmaInstituicao() {
        Doacao doacao = new Doacao("Maria", "Arroz", 5, "kg", LocalDate.now(), ID_INST);
        when(instituicaoRepository.buscarPorId(ID_INST)).thenReturn(Optional.of(instituicaoExistente()));
        when(repository.listarPorInstituicao(ID_INST)).thenReturn(List.of(doacao));

        List<Doacao> lista = service.listarPorInstituicao(ID_INST);

        assertEquals(1, lista.size());
        assertEquals(ID_INST, lista.get(0).getInstituicaoId());
    }

    @Test
    void deveLancarExcecaoAoListarDoacoesDeInstituicaoInexistente() {
        when(instituicaoRepository.buscarPorId("99")).thenReturn(Optional.empty());

        assertThrows(InstituicaoNaoEncontradaException.class, () -> service.listarPorInstituicao("99"));
        verifyNoInteractions(repository);
    }

    @Test
    void naoDeveRegistrarComDoadorVazio() {
        assertThrows(DoacaoInvalidaException.class,
                () -> service.registrar(" ", "Arroz", 5, "kg", LocalDate.now(), ID_INST));
        verifyNoInteractions(repository);
    }

    @Test
    void naoDeveRegistrarComItemVazio() {
        assertThrows(DoacaoInvalidaException.class,
                () -> service.registrar("Maria", "", 5, "kg", LocalDate.now(), ID_INST));
    }

    @Test
    void naoDeveRegistrarComQuantidadeInvalida() {
        assertThrows(DoacaoInvalidaException.class,
                () -> service.registrar("Maria", "Arroz", 0, "kg", LocalDate.now(), ID_INST));
    }

    @Test
    void naoDeveRegistrarComUnidadeVazia() {
        assertThrows(DoacaoInvalidaException.class,
                () -> service.registrar("Maria", "Arroz", 5, "", LocalDate.now(), ID_INST));
    }

    @Test
    void naoDeveRegistrarComDataNula() {
        assertThrows(DoacaoInvalidaException.class,
                () -> service.registrar("Maria", "Arroz", 5, "kg", null, ID_INST));
    }

    @Test
    void deveListarTodasAsDoacoes() {
        Doacao doacao = new Doacao("Maria", "Arroz", 5, "kg", LocalDate.now(), ID_INST);
        when(repository.listarTodas()).thenReturn(List.of(doacao));

        List<Doacao> lista = service.listar();

        assertEquals(1, lista.size());
        verify(repository).listarTodas();
    }

    @Test
    void deveBuscarPorIdExistente() {
        Doacao doacao = new Doacao("Maria", "Arroz", 5, "kg", LocalDate.now(), ID_INST);
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
