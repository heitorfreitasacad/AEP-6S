package br.com.unicesumar.aep.controller;

import br.com.unicesumar.aep.model.Doacao;
import br.com.unicesumar.aep.model.StatusDoacao;
import br.com.unicesumar.aep.service.DoacaoService;
import io.javalin.http.Context;

import java.time.LocalDate;

/**
 * Endpoints REST do CRUD de doacoes. So traduz HTTP <-> DoacaoService;
 * validacao e regra de negocio ficam no service.
 */
public class DoacaoController {

    private final DoacaoService service;

    public DoacaoController(DoacaoService service) {
        this.service = service;
    }

    public void registrar(Context ctx) {
        DoacaoRequest body = ctx.bodyAsClass(DoacaoRequest.class);
        Doacao doacao = service.registrar(body.doador(), body.item(), body.quantidade(), body.unidade(), body.dataDoacao());
        ctx.status(201).json(doacao);
    }

    public void listar(Context ctx) {
        ctx.json(service.listar());
    }

    public void buscarPorId(Context ctx) {
        Doacao doacao = service.buscarPorId(ctx.pathParam("id"));
        ctx.json(doacao);
    }

    public void atualizarStatus(Context ctx) {
        StatusRequest body = ctx.bodyAsClass(StatusRequest.class);
        service.atualizarStatus(ctx.pathParam("id"), body.status());
        ctx.status(204);
    }

    public void remover(Context ctx) {
        service.remover(ctx.pathParam("id"));
        ctx.status(204);
    }

    public record DoacaoRequest(String doador, String item, double quantidade, String unidade, LocalDate dataDoacao) {
    }

    public record StatusRequest(StatusDoacao status) {
    }
}
