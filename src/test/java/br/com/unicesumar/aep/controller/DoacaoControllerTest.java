package br.com.unicesumar.aep.controller;

import br.com.unicesumar.aep.controller.DoacaoController.DoacaoRequest;
import br.com.unicesumar.aep.controller.DoacaoController.StatusRequest;
import br.com.unicesumar.aep.model.Doacao;
import br.com.unicesumar.aep.model.StatusDoacao;
import br.com.unicesumar.aep.service.DoacaoService;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoacaoControllerTest {

    private static final LocalDate DATA = LocalDate.of(2026, 8, 20);

    @Mock
    private DoacaoService service;

    private DoacaoController controller;
    private Context ctx;

    @BeforeEach
    void setUp() {
        controller = new DoacaoController(service);
        // RETURNS_SELF permite o encadeamento ctx.status(..).json(..)
        ctx = mock(Context.class, RETURNS_SELF);
    }

    @Test
    void registrarDeveRepassarInstituicaoIdAoServiceEResponder201() {
        Doacao criada = new Doacao("Maria", "Arroz", 10, "kg", DATA, "inst1");
        criada.setId("1");
        when(ctx.bodyAsClass(DoacaoRequest.class))
                .thenReturn(new DoacaoRequest("Maria", "Arroz", 10, "kg", DATA, "inst1"));
        when(service.registrar("Maria", "Arroz", 10, "kg", DATA, "inst1")).thenReturn(criada);

        controller.registrar(ctx);

        verify(ctx).status(201);
        verify(ctx).json(criada);
    }

    @Test
    void listarDeveResponderComTodasAsDoacoes() {
        List<Doacao> lista = List.of(new Doacao("Maria", "Arroz", 10, "kg", DATA, "inst1"));
        when(service.listar()).thenReturn(lista);

        controller.listar(ctx);

        verify(ctx).json(lista);
    }

    @Test
    void listarPorInstituicaoDeveUsarOIdDoCaminho() {
        List<Doacao> lista = List.of(new Doacao("Maria", "Arroz", 10, "kg", DATA, "inst1"));
        when(ctx.pathParam("id")).thenReturn("inst1");
        when(service.listarPorInstituicao("inst1")).thenReturn(lista);

        controller.listarPorInstituicao(ctx);

        verify(ctx).json(lista);
    }

    @Test
    void buscarPorIdDeveResponderComADoacao() {
        Doacao doacao = new Doacao("Maria", "Arroz", 10, "kg", DATA, "inst1");
        when(ctx.pathParam("id")).thenReturn("1");
        when(service.buscarPorId("1")).thenReturn(doacao);

        controller.buscarPorId(ctx);

        verify(ctx).json(doacao);
    }

    @Test
    void atualizarStatusDeveResponder204() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.bodyAsClass(StatusRequest.class)).thenReturn(new StatusRequest(StatusDoacao.RECEBIDA));

        controller.atualizarStatus(ctx);

        verify(service).atualizarStatus("1", StatusDoacao.RECEBIDA);
        verify(ctx).status(204);
    }

    @Test
    void removerDeveResponder204() {
        when(ctx.pathParam("id")).thenReturn("1");

        controller.remover(ctx);

        verify(service).remover("1");
        verify(ctx).status(204);
    }
}
