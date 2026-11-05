package br.com.unicesumar.aep.service;

import br.com.unicesumar.aep.exception.InstituicaoInvalidaException;
import br.com.unicesumar.aep.exception.InstituicaoNaoEncontradaException;
import br.com.unicesumar.aep.model.Contato;
import br.com.unicesumar.aep.model.Endereco;
import br.com.unicesumar.aep.model.Instituicao;
import br.com.unicesumar.aep.repository.InstituicaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class InstituicaoServiceTest {

    @Mock
    private InstituicaoRepository repository;

    private InstituicaoService service;

    private final Endereco endereco = new Endereco("Rua A", "Maringa", "PR");
    private final List<Contato> contatos = List.of(new Contato("Ana", "44999990000", "ana@ong.org"));

    @BeforeEach
    void setUp() {
        service = new InstituicaoService(repository);
    }

    @Test
    void deveRegistrarInstituicaoValida() {
        when(repository.salvar(any(Instituicao.class))).thenAnswer(invocacao -> {
            Instituicao instituicao = invocacao.getArgument(0);
            instituicao.setId("1");
            return instituicao;
        });

        Instituicao instituicao = service.registrar("Banco de Alimentos", endereco, contatos);

        assertEquals("1", instituicao.getId());
        assertEquals(1, instituicao.getContatos().size());
        verify(repository).salvar(any(Instituicao.class));
    }

    @Test
    void deveRegistrarInstituicaoSemContatos() {
        when(repository.salvar(any(Instituicao.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        Instituicao instituicao = service.registrar("ONG", endereco, null);

        assertEquals(0, instituicao.getContatos().size());
    }

    @Test
    void naoDeveRegistrarComNomeVazio() {
        assertThrows(InstituicaoInvalidaException.class, () -> service.registrar(" ", endereco, contatos));
        verifyNoInteractions(repository);
    }

    @Test
    void naoDeveRegistrarSemEndereco() {
        assertThrows(InstituicaoInvalidaException.class, () -> service.registrar("ONG", null, contatos));
    }

    @Test
    void naoDeveRegistrarComEnderecoIncompleto() {
        Endereco semCidade = new Endereco("Rua A", "", "PR");

        assertThrows(InstituicaoInvalidaException.class, () -> service.registrar("ONG", semCidade, contatos));
    }

    @Test
    void naoDeveRegistrarComContatoSemNome() {
        List<Contato> invalidos = List.of(new Contato("", "1", "a@a.com"));

        assertThrows(InstituicaoInvalidaException.class, () -> service.registrar("ONG", endereco, invalidos));
    }

    @Test
    void deveListarTodasAsInstituicoes() {
        when(repository.listarTodas()).thenReturn(List.of(new Instituicao("ONG", endereco, contatos)));

        assertEquals(1, service.listar().size());
    }

    @Test
    void deveBuscarPorIdExistente() {
        Instituicao instituicao = new Instituicao("ONG", endereco, contatos);
        instituicao.setId("1");
        when(repository.buscarPorId("1")).thenReturn(Optional.of(instituicao));

        assertEquals("1", service.buscarPorId("1").getId());
    }

    @Test
    void deveLancarExcecaoAoBuscarIdInexistente() {
        when(repository.buscarPorId("99")).thenReturn(Optional.empty());

        assertThrows(InstituicaoNaoEncontradaException.class, () -> service.buscarPorId("99"));
    }

    @Test
    void deveAtualizarInstituicaoExistente() {
        when(repository.atualizar(any(Instituicao.class))).thenReturn(true);

        Instituicao atualizada = service.atualizar("1", "Novo Nome", endereco, contatos);

        assertEquals("1", atualizada.getId());
        assertEquals("Novo Nome", atualizada.getNome());
    }

    @Test
    void deveLancarExcecaoAoAtualizarInexistente() {
        when(repository.atualizar(any(Instituicao.class))).thenReturn(false);

        assertThrows(InstituicaoNaoEncontradaException.class,
                () -> service.atualizar("99", "ONG", endereco, contatos));
    }

    @Test
    void deveRemoverComSucesso() {
        when(repository.remover("1")).thenReturn(true);

        assertDoesNotThrow(() -> service.remover("1"));
    }

    @Test
    void deveLancarExcecaoAoRemoverInexistente() {
        when(repository.remover("99")).thenReturn(false);

        assertThrows(InstituicaoNaoEncontradaException.class, () -> service.remover("99"));
    }
}
