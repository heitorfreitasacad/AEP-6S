package br.com.unicesumar.aep.repository;

import br.com.unicesumar.aep.model.Contato;
import br.com.unicesumar.aep.model.Endereco;
import br.com.unicesumar.aep.model.Instituicao;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementacao MongoDB do repositorio de instituicoes.
 * 2a entrega: colecao "instituicoes" com documento aninhado (endereco)
 * e lista de subdocumentos (contatos).
 */
public class InstituicaoMongoRepository implements InstituicaoRepository {

    private static final String COLLECTION_NAME = "instituicoes";

    private final MongoCollection<Document> collection;

    public InstituicaoMongoRepository(MongoDatabase database) {
        this.collection = database.getCollection(COLLECTION_NAME);
    }

    @Override
    public Instituicao salvar(Instituicao instituicao) {
        Document documento = toDocument(instituicao);
        collection.insertOne(documento);
        instituicao.setId(documento.getObjectId("_id").toHexString());
        return instituicao;
    }

    @Override
    public Optional<Instituicao> buscarPorId(String id) {
        if (!ObjectId.isValid(id)) {
            return Optional.empty();
        }
        Document documento = collection.find(Filters.eq("_id", new ObjectId(id))).first();
        return Optional.ofNullable(documento).map(this::toInstituicao);
    }

    @Override
    public List<Instituicao> listarTodas() {
        List<Instituicao> instituicoes = new ArrayList<>();
        for (Document documento : collection.find()) {
            instituicoes.add(toInstituicao(documento));
        }
        return instituicoes;
    }

    @Override
    public boolean atualizar(Instituicao instituicao) {
        if (!ObjectId.isValid(instituicao.getId())) {
            return false;
        }
        UpdateResult resultado = collection.replaceOne(
                Filters.eq("_id", new ObjectId(instituicao.getId())),
                toDocument(instituicao));
        return resultado.getMatchedCount() > 0;
    }

    @Override
    public boolean remover(String id) {
        if (!ObjectId.isValid(id)) {
            return false;
        }
        DeleteResult resultado = collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
        return resultado.getDeletedCount() > 0;
    }

    private Document toDocument(Instituicao instituicao) {
        Endereco endereco = instituicao.getEndereco();
        Document enderecoDoc = new Document()
                .append("rua", endereco.getRua())
                .append("cidade", endereco.getCidade())
                .append("estado", endereco.getEstado());

        List<Document> contatosDoc = new ArrayList<>();
        for (Contato contato : instituicao.getContatos()) {
            contatosDoc.add(new Document()
                    .append("nome", contato.getNome())
                    .append("telefone", contato.getTelefone())
                    .append("email", contato.getEmail()));
        }

        return new Document()
                .append("nome", instituicao.getNome())
                .append("endereco", enderecoDoc)
                .append("contatos", contatosDoc);
    }

    private Instituicao toInstituicao(Document documento) {
        Document enderecoDoc = documento.get("endereco", Document.class);
        Endereco endereco = new Endereco(
                enderecoDoc.getString("rua"),
                enderecoDoc.getString("cidade"),
                enderecoDoc.getString("estado"));

        List<Contato> contatos = new ArrayList<>();
        for (Document contatoDoc : documento.getList("contatos", Document.class, new ArrayList<>())) {
            contatos.add(new Contato(
                    contatoDoc.getString("nome"),
                    contatoDoc.getString("telefone"),
                    contatoDoc.getString("email")));
        }

        Instituicao instituicao = new Instituicao(documento.getString("nome"), endereco, contatos);
        instituicao.setId(documento.getObjectId("_id").toHexString());
        return instituicao;
    }
}
