package br.com.unicesumar.aep.controller;

import br.com.unicesumar.aep.controller.InstituicaoController.InstituicaoRequest;
import br.com.unicesumar.aep.model.Contato;
import br.com.unicesumar.aep.model.Endereco;
import br.com.unicesumar.aep.model.Instituicao;
import br.com.unicesumar.aep.service.InstituicaoService;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstituicaoControllerTest {

    @Mock
    private InstituicaoService service;

    private InstituicaoController controller;
    private Context ctx;

    private final Endereco endereco = new Endereco("Rua A", "Maringa", "PR");
    private final List<Contato> contatos = List.of(new Contato("Ana", "44999990000", "ana@ong.org"));

    @BeforeEach
    void setUp() {
        controller = new InstituicaoController(service);
        // RETURNS_SELF permite o encadeamento ctx.status(..).json(..)
        ctx = mock(Context.class, RETURNS_SELF);
    }

    @Test
    void registrarDeveResponder201ComAInstituicaoCriada() {
        Instituicao criada = new Instituicao("Banco de Alimentos", endereco, contatos);
        criada.setId("1");
        when(ctx.bodyAsClass(InstituicaoRequest.class))
                .thenReturn(new InstituicaoRequest("Banco de Alimentos", endereco, contatos));
        when(service.registrar("Banco de Alimentos", endereco, contatos)).thenReturn(criada);

        controller.registrar(ctx);

        verify(ctx).status(201);
        verify(ctx).json(criada);
    }

    @Test
    void listarDeveResponderComTodasAsInstituicoes() {
        List<Instituicao> lista = List.of(new Instituicao("ONG", endereco, contatos));
        when(service.listar()).thenReturn(lista);

        controller.listar(ctx);

        verify(ctx).json(lista);
    }

    @Test
    void buscarPorIdDeveResponderComAInstituicao() {
        Instituicao instituicao = new Instituicao("ONG", endereco, contatos);
        when(ctx.pathParam("id")).thenReturn("1");
        when(service.buscarPorId("1")).thenReturn(instituicao);

        controller.buscarPorId(ctx);

        verify(ctx).json(instituicao);
    }

    @Test
    void atualizarDeveResponderComAInstituicaoAtualizada() {
        Instituicao atualizada = new Instituicao("Novo Nome", endereco, contatos);
        atualizada.setId("1");
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.bodyAsClass(InstituicaoRequest.class))
                .thenReturn(new InstituicaoRequest("Novo Nome", endereco, contatos));
        when(service.atualizar("1", "Novo Nome", endereco, contatos)).thenReturn(atualizada);

        controller.atualizar(ctx);

        verify(ctx).json(atualizada);
    }

    @Test
    void removerDeveResponder204() {
        when(ctx.pathParam("id")).thenReturn("1");

        controller.remover(ctx);

        verify(service).remover("1");
        verify(ctx).status(204);
    }
}
