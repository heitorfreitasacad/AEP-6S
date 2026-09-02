package br.com.unicesumar.aep.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * Ponto unico de acesso ao MongoDB.
 * URI e nome do banco configuraveis por variavel de ambiente
 * (MONGODB_URI / MONGODB_DATABASE), com fallback para uma instancia local.
 */
public final class MongoConfig {

    private static final String DEFAULT_URI = "mongodb://localhost:27017";
    private static final String DEFAULT_DATABASE = "aep_doacoes";

    private static MongoClient client;

    private MongoConfig() {
    }

    public static synchronized MongoDatabase getDatabase() {
        if (client == null) {
            String uri = System.getenv().getOrDefault("MONGODB_URI", DEFAULT_URI);
            client = MongoClients.create(uri);
        }
        String database = System.getenv().getOrDefault("MONGODB_DATABASE", DEFAULT_DATABASE);
        return client.getDatabase(database);
    }
}
