package br.com.unicesumar.aep.repository;

import br.com.unicesumar.aep.model.Doacao;
import br.com.unicesumar.aep.model.StatusDoacao;
import br.com.unicesumar.aep.support.MongoEmMemoria;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DoacaoMongoRepositoryTest {

    private static MongoEmMemoria mongo;

    private MongoDatabase database;
    private DoacaoMongoRepository repository;
    private final String instituicaoId = new ObjectId().toHexString();

    @BeforeAll
    static void iniciarMongo() {
        mongo = new MongoEmMemoria();
    }

    @AfterAll
    static void encerrarMongo() {
        mongo.close();
    }

    @BeforeEach
    void setUp() {
        database = mongo.novoBanco();
        repository = new DoacaoMongoRepository(database);
    }

    private Doacao doacao(String doador, String destino) {
        return new Doacao(doador, "Arroz", 10, "kg", LocalDate.of(2026, 8, 20), destino);
    }

    @Test
    void deveSalvarDoacaoGerandoIdEGuardandoInstituicaoComoObjectId() {
        Doacao salva = repository.salvar(doacao("Maria", instituicaoId));

        assertNotNull(salva.getId());
        Document bruto = database.getCollection("doacoes").find().first();
        assertEquals(new ObjectId(instituicaoId), bruto.getObjectId("instituicaoId"));
        assertEquals("PENDENTE", bruto.getString("status"));
        assertEquals("2026-08-20", bruto.getString("dataDoacao"));
    }

    @Test
    void deveBuscarDoacaoPorId() {
        Doacao salva = repository.salvar(doacao("Maria", instituicaoId));

        Optional<Doacao> encontrada = repository.buscarPorId(salva.getId());

        assertTrue(encontrada.isPresent());
        assertEquals("Maria", encontrada.get().getDoador());
        assertEquals(10.0, encontrada.get().getQuantidade());
        assertEquals(LocalDate.of(2026, 8, 20), encontrada.get().getDataDoacao());
        assertEquals(StatusDoacao.PENDENTE, encontrada.get().getStatus());
        assertEquals(instituicaoId, encontrada.get().getInstituicaoId());
    }

    @Test
    void deveRetornarVazioParaIdInexistenteOuInvalido() {
        assertTrue(repository.buscarPorId(new ObjectId().toHexString()).isEmpty());
        assertTrue(repository.buscarPorId("id-invalido").isEmpty());
    }

    @Test
    void deveListarTodasAsDoacoes() {
        repository.salvar(doacao("Maria", instituicaoId));
        repository.salvar(doacao("Joao", instituicaoId));

        assertEquals(2, repository.listarTodas().size());
    }

    @Test
    void deveListarApenasDoacoesDaInstituicao() {
        String outraInstituicao = new ObjectId().toHexString();
        repository.salvar(doacao("Maria", instituicaoId));
        repository.salvar(doacao("Joao", outraInstituicao));

        List<Doacao> daInstituicao = repository.listarPorInstituicao(instituicaoId);

        assertEquals(1, daInstituicao.size());
        assertEquals("Maria", daInstituicao.get(0).getDoador());
    }

    @Test
    void deveRetornarListaVaziaParaInstituicaoComIdInvalido() {
        assertTrue(repository.listarPorInstituicao("id-invalido").isEmpty());
    }

    @Test
    void deveAtualizarStatus() {
        Doacao salva = repository.salvar(doacao("Maria", instituicaoId));

        assertTrue(repository.atualizarStatus(salva.getId(), StatusDoacao.DISTRIBUIDA));

        assertEquals(StatusDoacao.DISTRIBUIDA, repository.buscarPorId(salva.getId()).orElseThrow().getStatus());
    }

    @Test
    void naoDeveAtualizarStatusDeDoacaoInexistenteOuIdInvalido() {
        assertFalse(repository.atualizarStatus(new ObjectId().toHexString(), StatusDoacao.RECEBIDA));
        assertFalse(repository.atualizarStatus("id-invalido", StatusDoacao.RECEBIDA));
    }

    @Test
    void deveRemoverDoacao() {
        Doacao salva = repository.salvar(doacao("Maria", instituicaoId));

        assertTrue(repository.remover(salva.getId()));

        assertTrue(repository.buscarPorId(salva.getId()).isEmpty());
    }

    @Test
    void naoDeveRemoverDoacaoInexistenteOuIdInvalido() {
        assertFalse(repository.remover(new ObjectId().toHexString()));
        assertFalse(repository.remover("id-invalido"));
    }

    @Test
    void deveLerDoacaoDaPrimeiraEntregaSemInstituicao() {
        database.getCollection("doacoes").insertOne(new Document()
                .append("doador", "Legado")
                .append("item", "Feijao")
                .append("quantidade", 2.0)
                .append("unidade", "kg")
                .append("dataDoacao", "2026-08-01")
                .append("status", "PENDENTE"));

        Doacao legada = repository.listarTodas().get(0);

        assertEquals("Legado", legada.getDoador());
        assertNull(legada.getInstituicaoId());
    }

    @Test
    void deveSalvarDoacaoSemInstituicaoSemCriarOCampo() {
        repository.salvar(doacao("Maria", null));

        Document bruto = database.getCollection("doacoes").find().first();
        assertFalse(bruto.containsKey("instituicaoId"));
    }
}
