package br.com.unicesumar.aep.support;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * Servidor MongoDB em memoria para testes: nao precisa de Docker nem de
 * mongod instalado, entao os testes rodam em qualquer maquina.
 */
public class MongoEmMemoria implements AutoCloseable {

    private final MongoServer server = new MongoServer(new MemoryBackend());
    private final MongoClient client;

    public MongoEmMemoria() {
        InetSocketAddress endereco = server.bind();
        client = MongoClients.create("mongodb://" + endereco.getHostName() + ":" + endereco.getPort());
    }

    public MongoDatabase novoBanco() {
        return client.getDatabase("teste_" + UUID.randomUUID().toString().replace("-", ""));
    }

    @Override
    public void close() {
        client.close();
        server.shutdown();
    }
}
