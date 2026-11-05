package br.com.unicesumar.aep.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ONG ou banco de alimentos que recebe doacoes. Documento complexo da
 * colecao "instituicoes": possui um endereco aninhado e uma lista de
 * contatos (subdocumentos).
 */
public class Instituicao {

    private String id;
    private String nome;
    private Endereco endereco;
    private List<Contato> contatos = new ArrayList<>();

    public Instituicao() {
    }

    public Instituicao(String nome, Endereco endereco, List<Contato> contatos) {
        this.nome = nome;
        this.endereco = endereco;
        if (contatos != null) {
            this.contatos = contatos;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public List<Contato> getContatos() {
        return contatos;
    }

    public void setContatos(List<Contato> contatos) {
        this.contatos = contatos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Instituicao)) return false;
        Instituicao that = (Instituicao) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Instituicao{" +
                "id='" + id + '\'' +
                ", nome='" + nome + '\'' +
                ", endereco=" + endereco +
                ", contatos=" + contatos +
                '}';
    }
}
