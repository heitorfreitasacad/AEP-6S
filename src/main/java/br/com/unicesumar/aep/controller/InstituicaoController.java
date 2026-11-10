package br.com.unicesumar.aep.controller;

import br.com.unicesumar.aep.model.Contato;
import br.com.unicesumar.aep.model.Endereco;
import br.com.unicesumar.aep.model.Instituicao;
import br.com.unicesumar.aep.service.InstituicaoService;
import io.javalin.http.Context;

import java.util.List;

/**
 * Endpoints REST do CRUD de instituicoes. So traduz HTTP <-> InstituicaoService;
 * validacao e regra de negocio ficam no service.
 */
public class InstituicaoController {

    private final InstituicaoService service;

    public InstituicaoController(InstituicaoService service) {
        this.service = service;
    }

    public void registrar(Context ctx) {
        InstituicaoRequest body = ctx.bodyAsClass(InstituicaoRequest.class);
        Instituicao instituicao = service.registrar(body.nome(), body.endereco(), body.contatos());
        ctx.status(201).json(instituicao);
    }

    public void listar(Context ctx) {
        ctx.json(service.listar());
    }

    public void buscarPorId(Context ctx) {
        ctx.json(service.buscarPorId(ctx.pathParam("id")));
    }

    public void atualizar(Context ctx) {
        InstituicaoRequest body = ctx.bodyAsClass(InstituicaoRequest.class);
        Instituicao instituicao = service.atualizar(ctx.pathParam("id"), body.nome(), body.endereco(), body.contatos());
        ctx.json(instituicao);
    }

    public void remover(Context ctx) {
        service.remover(ctx.pathParam("id"));
        ctx.status(204);
    }

    public record InstituicaoRequest(String nome, Endereco endereco, List<Contato> contatos) {
    }
}
