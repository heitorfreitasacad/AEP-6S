package br.com.unicesumar.aep.repository;

import br.com.unicesumar.aep.model.Contato;
import br.com.unicesumar.aep.model.Endereco;
import br.com.unicesumar.aep.model.Instituicao;
import br.com.unicesumar.aep.support.MongoEmMemoria;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstituicaoMongoRepositoryTest {

    private static MongoEmMemoria mongo;

    private MongoDatabase database;
    private InstituicaoMongoRepository repository;

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
        repository = new InstituicaoMongoRepository(database);
    }

    private Instituicao instituicao(String nome) {
        return new Instituicao(nome,
                new Endereco("Rua A", "Maringa", "PR"),
                List.of(new Contato("Ana", "44999990000", "ana@ong.org"),
                        new Contato("Carlos", "44988880000", "carlos@ong.org")));
    }

    @Test
    void deveSalvarEnderecoComoDocumentoAninhadoEContatosComoLista() {
        Instituicao salva = repository.salvar(instituicao("Banco de Alimentos"));

        assertNotNull(salva.getId());
        Document bruto = database.getCollection("instituicoes").find().first();
        Document endereco = bruto.get("endereco", Document.class);
        assertEquals("Maringa", endereco.getString("cidade"));
        List<Document> contatos = bruto.getList("contatos", Document.class);
        assertEquals(2, contatos.size());
        assertEquals("Carlos", contatos.get(1).getString("nome"));
    }

    @Test
    void deveBuscarInstituicaoCompletaPorId() {
        Instituicao salva = repository.salvar(instituicao("Banco de Alimentos"));

        Optional<Instituicao> encontrada = repository.buscarPorId(salva.getId());

        assertTrue(encontrada.isPresent());
        assertEquals("Banco de Alimentos", encontrada.get().getNome());
        assertEquals("PR", encontrada.get().getEndereco().getEstado());
        assertEquals(2, encontrada.get().getContatos().size());
        assertEquals("ana@ong.org", encontrada.get().getContatos().get(0).getEmail());
    }

    @Test
    void deveRetornarVazioParaIdInexistenteOuInvalido() {
        assertTrue(repository.buscarPorId(new ObjectId().toHexString()).isEmpty());
        assertTrue(repository.buscarPorId("id-invalido").isEmpty());
    }

    @Test
    void deveListarTodasAsInstituicoes() {
        repository.salvar(instituicao("A"));
        repository.salvar(instituicao("B"));

        assertEquals(2, repository.listarTodas().size());
    }

    @Test
    void deveLerInstituicaoSemListaDeContatos() {
        database.getCollection("instituicoes").insertOne(new Document()
                .append("nome", "Sem contatos")
                .append("endereco", new Document("rua", "R").append("cidade", "C").append("estado", "PR")));

        Instituicao lida = repository.listarTodas().get(0);

        assertTrue(lida.getContatos().isEmpty());
    }

    @Test
    void deveAtualizarSubstituindoNomeEnderecoEContatos() {
        Instituicao salva = repository.salvar(instituicao("Antigo"));
        Instituicao nova = new Instituicao("Novo", new Endereco("Rua B", "Londrina", "PR"), List.of());
        nova.setId(salva.getId());

        assertTrue(repository.atualizar(nova));

        Instituicao lida = repository.buscarPorId(salva.getId()).orElseThrow();
        assertEquals("Novo", lida.getNome());
        assertEquals("Londrina", lida.getEndereco().getCidade());
        assertTrue(lida.getContatos().isEmpty());
    }

    @Test
    void naoDeveAtualizarInstituicaoInexistenteOuComIdInvalido() {
        Instituicao inexistente = instituicao("X");
        inexistente.setId(new ObjectId().toHexString());
        Instituicao invalida = instituicao("Y");
        invalida.setId("id-invalido");

        assertFalse(repository.atualizar(inexistente));
        assertFalse(repository.atualizar(invalida));
    }

    @Test
    void deveRemoverInstituicao() {
        Instituicao salva = repository.salvar(instituicao("A"));

        assertTrue(repository.remover(salva.getId()));

        assertTrue(repository.buscarPorId(salva.getId()).isEmpty());
    }

    @Test
    void naoDeveRemoverInstituicaoInexistenteOuIdInvalido() {
        assertFalse(repository.remover(new ObjectId().toHexString()));
        assertFalse(repository.remover("id-invalido"));
    }
}
