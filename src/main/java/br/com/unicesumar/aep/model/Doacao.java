package br.com.unicesumar.aep.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Doacao de alimentos feita por um doador.
 * Documento homogeneo de estrutura simples (1a entrega: colecao unica "doacoes").
 */
public class Doacao {

    private String id;
    private String doador;
    private String item;
    private double quantidade;
    private String unidade;
    private LocalDate dataDoacao;
    private StatusDoacao status;

    public Doacao() {
    }

    public Doacao(String doador, String item, double quantidade, String unidade, LocalDate dataDoacao) {
        this.doador = doador;
        this.item = item;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.dataDoacao = dataDoacao;
        this.status = StatusDoacao.PENDENTE;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDoador() {
        return doador;
    }

    public void setDoador(String doador) {
        this.doador = doador;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public LocalDate getDataDoacao() {
        return dataDoacao;
    }

    public void setDataDoacao(LocalDate dataDoacao) {
        this.dataDoacao = dataDoacao;
    }

    public StatusDoacao getStatus() {
        return status;
    }

    public void setStatus(StatusDoacao status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Doacao)) return false;
        Doacao doacao = (Doacao) o;
        return Objects.equals(id, doacao.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Doacao{" +
                "id='" + id + '\'' +
                ", doador='" + doador + '\'' +
                ", item='" + item + '\'' +
                ", quantidade=" + quantidade +
                ", unidade='" + unidade + '\'' +
                ", dataDoacao=" + dataDoacao +
                ", status=" + status +
                '}';
    }
}
