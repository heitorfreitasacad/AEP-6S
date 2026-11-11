package br.com.unicesumar.aep;

import br.com.unicesumar.aep.support.MongoEmMemoria;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.testtools.HttpClient;
import io.javalin.testtools.JavalinTest;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa a API de ponta a ponta: HTTP -> controller -> service -> repositorio
 * -> MongoDB (em memoria).
 */
class RestAppTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static MongoEmMemoria mongo;

    @BeforeAll
    static void iniciarMongo() {
        mongo = new MongoEmMemoria();
    }

    @AfterAll
    static void encerrarMongo() {
        mongo.close();
    }

    private Map<String, Object> corpoInstituicao(String nome) {
        return Map.of(
                "nome", nome,
                "endereco", Map.of("rua", "Rua A", "cidade", "Maringa", "estado", "PR"),
                "contatos", List.of(Map.of("nome", "Ana", "telefone", "44999990000", "email", "ana@ong.org")));
    }

    private Map<String, Object> corpoDoacao(String doador, String instituicaoId) {
        return Map.of("doador", doador, "item", "Arroz", "quantidade", 10, "unidade", "kg",
                "dataDoacao", "2026-08-20", "instituicaoId", instituicaoId);
    }

    private JsonNode json(okhttp3.Response resposta) throws IOException {
        return JSON.readTree(resposta.body().string());
    }

    private String criarInstituicao(HttpClient client, String nome) throws IOException {
        okhttp3.Response resposta = client.post("/instituicoes", corpoInstituicao(nome));
        assertEquals(201, resposta.code());
        return json(resposta).get("id").asText();
    }

    @Test
    void deveExecutarOFluxoCompletoComRelacionamentoEntreColecoes() {
        JavalinTest.test(RestApp.criarApp(mongo.novoBanco()), (server, client) -> {
            String instituicaoId = criarInstituicao(client, "Banco de Alimentos");

            okhttp3.Response criada = client.post("/doacoes", corpoDoacao("Maria", instituicaoId));
            assertEquals(201, criada.code());
            JsonNode doacao = json(criada);
            String doacaoId = doacao.get("id").asText();
            assertEquals("PENDENTE", doacao.get("status").asText());
            assertEquals("2026-08-20", doacao.get("dataDoacao").asText());
            assertEquals(instituicaoId, doacao.get("instituicaoId").asText());

            JsonNode daInstituicao = json(client.get("/instituicoes/" + instituicaoId + "/doacoes"));
            assertEquals(1, daInstituicao.size());
            assertEquals("Maria", daInstituicao.get(0).get("doador").asText());

            assertEquals(204, client.patch("/doacoes/" + doacaoId + "/status", Map.of("status", "RECEBIDA")).code());
            assertEquals("RECEBIDA", json(client.get("/doacoes/" + doacaoId)).get("status").asText());
            assertEquals(1, json(client.get("/doacoes")).size());

            assertEquals(204, client.delete("/doacoes/" + doacaoId).code());
            assertEquals(404, client.get("/doacoes/" + doacaoId).code());
        });
    }

    @Test
    void deveRetornarInstituicaoComEnderecoEContatosAninhados() {
        JavalinTest.test(RestApp.criarApp(mongo.novoBanco()), (server, client) -> {
            String id = criarInstituicao(client, "ONG Esperanca");

            JsonNode instituicao = json(client.get("/instituicoes/" + id));

            assertEquals("Maringa", instituicao.get("endereco").get("cidade").asText());
            assertEquals(1, instituicao.get("contatos").size());
            assertEquals("Ana", instituicao.get("contatos").get(0).get("nome").asText());
            assertEquals(1, json(client.get("/instituicoes")).size());
        });
    }

    @Test
    void deveAtualizarERemoverInstituicao() {
        JavalinTest.test(RestApp.criarApp(mongo.novoBanco()), (server, client) -> {
            String id = criarInstituicao(client, "Antigo");

            okhttp3.Response atualizada = client.put("/instituicoes/" + id, corpoInstituicao("Novo"));
            assertEquals(200, atualizada.code());
            assertEquals("Novo", json(client.get("/instituicoes/" + id)).get("nome").asText());

            assertEquals(204, client.delete("/instituicoes/" + id).code());
            assertEquals(404, client.get("/instituicoes/" + id).code());
            assertEquals(404, client.delete("/instituicoes/" + id).code());
        });
    }

    @Test
    void deveRetornar400QuandoDoacaoApontaParaInstituicaoInexistente() {
        JavalinTest.test(RestApp.criarApp(mongo.novoBanco()), (server, client) -> {
            okhttp3.Response resposta = client.post("/doacoes", corpoDoacao("Maria", new ObjectId().toHexString()));

            assertEquals(400, resposta.code());
            assertTrue(json(resposta).get("erro").asText().contains("Instituicao"));
        });
    }

    @Test
    void deveRetornar400ParaDadosInvalidos() {
        JavalinTest.test(RestApp.criarApp(mongo.novoBanco()), (server, client) -> {
            Map<String, Object> semNome = Map.of(
                    "nome", " ",
                    "endereco", Map.of("rua", "A", "cidade", "B", "estado", "PR"));
            assertEquals(400, client.post("/instituicoes", semNome).code());

            String instituicaoId = criarInstituicao(client, "ONG");
            Map<String, Object> dataFutura = Map.of("doador", "Maria", "item", "Arroz", "quantidade", 1,
                    "unidade", "kg", "dataDoacao", "2999-01-01", "instituicaoId", instituicaoId);
            assertEquals(400, client.post("/doacoes", dataFutura).code());
        });
    }

    @Test
    void deveRetornar404ParaRecursosInexistentes() {
        JavalinTest.test(RestApp.criarApp(mongo.novoBanco()), (server, client) -> {
            String idInexistente = new ObjectId().toHexString();

            assertEquals(404, client.get("/doacoes/" + idInexistente).code());
            assertEquals(404, client.delete("/doacoes/" + idInexistente).code());
            assertEquals(404, client.get("/instituicoes/" + idInexistente).code());
            assertEquals(404, client.get("/instituicoes/" + idInexistente + "/doacoes").code());
            assertEquals(404, client.patch("/doacoes/" + idInexistente + "/status",
                    Map.of("status", "RECEBIDA")).code());
        });
    }

    @Test
    void deveServirSwaggerUiEEspecificacaoOpenApi() {
        JavalinTest.test(RestApp.criarApp(mongo.novoBanco()), (server, client) -> {
            assertEquals(200, client.get("/swagger-ui.html").code());
            okhttp3.Response spec = client.get("/openapi.yaml");
            assertEquals(200, spec.code());
            assertTrue(spec.body().string().contains("/instituicoes"));
        });
    }
}
