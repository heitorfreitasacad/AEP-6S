package br.com.unicesumar.aep.repository;

import br.com.unicesumar.aep.model.Doacao;
import br.com.unicesumar.aep.model.StatusDoacao;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementacao MongoDB do repositorio de doacoes.
 * 1a entrega: colecao unica "doacoes" com documentos homogeneos.
 */
public class DoacaoMongoRepository implements DoacaoRepository {

    private static final String COLLECTION_NAME = "doacoes";

    private final MongoCollection<Document> collection;

    public DoacaoMongoRepository(MongoDatabase database) {
        this.collection = database.getCollection(COLLECTION_NAME);
    }

    @Override
    public Doacao salvar(Doacao doacao) {
        Document documento = toDocument(doacao);
        collection.insertOne(documento);
        doacao.setId(documento.getObjectId("_id").toHexString());
        return doacao;
    }

    @Override
    public Optional<Doacao> buscarPorId(String id) {
        if (!ObjectId.isValid(id)) {
            return Optional.empty();
        }
        Document documento = collection.find(Filters.eq("_id", new ObjectId(id))).first();
        return Optional.ofNullable(documento).map(this::toDoacao);
    }

    @Override
    public List<Doacao> listarTodas() {
        List<Doacao> doacoes = new ArrayList<>();
        for (Document documento : collection.find()) {
            doacoes.add(toDoacao(documento));
        }
        return doacoes;
    }

    @Override
    public boolean atualizarStatus(String id, StatusDoacao status) {
        if (!ObjectId.isValid(id)) {
            return false;
        }
        UpdateResult resultado = collection.updateOne(
                Filters.eq("_id", new ObjectId(id)),
                Updates.set("status", status.name()));
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

    private Document toDocument(Doacao doacao) {
        return new Document()
                .append("doador", doacao.getDoador())
                .append("item", doacao.getItem())
                .append("quantidade", doacao.getQuantidade())
                .append("unidade", doacao.getUnidade())
                .append("dataDoacao", doacao.getDataDoacao().toString())
                .append("status", doacao.getStatus().name());
    }

    private Doacao toDoacao(Document documento) {
        Doacao doacao = new Doacao();
        doacao.setId(documento.getObjectId("_id").toHexString());
        doacao.setDoador(documento.getString("doador"));
        doacao.setItem(documento.getString("item"));
        doacao.setQuantidade(documento.getDouble("quantidade"));
        doacao.setUnidade(documento.getString("unidade"));
        doacao.setDataDoacao(LocalDate.parse(documento.getString("dataDoacao")));
        doacao.setStatus(StatusDoacao.valueOf(documento.getString("status")));
        return doacao;
    }
}
