package br.com.unicesumar.aep;

import br.com.unicesumar.aep.config.MongoConfig;
import br.com.unicesumar.aep.controller.DoacaoController;
import br.com.unicesumar.aep.controller.InstituicaoController;
import br.com.unicesumar.aep.exception.DoacaoInvalidaException;
import br.com.unicesumar.aep.exception.DoacaoNaoEncontradaException;
import br.com.unicesumar.aep.exception.InstituicaoInvalidaException;
import br.com.unicesumar.aep.exception.InstituicaoNaoEncontradaException;
import br.com.unicesumar.aep.repository.DoacaoMongoRepository;
import br.com.unicesumar.aep.repository.InstituicaoMongoRepository;
import br.com.unicesumar.aep.service.DoacaoService;
import br.com.unicesumar.aep.service.InstituicaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;

import java.util.Map;

/**
 * Ponto de entrada da API REST. Endpoints em /doacoes e /instituicoes, documentacao
 * Swagger UI servida em /swagger-ui.html (spec estatica em /openapi.yaml).
 */
public class RestApp {

    public static void main(String[] args) {
        InstituicaoMongoRepository instituicaoRepository = new InstituicaoMongoRepository(MongoConfig.getDatabase());
        DoacaoService service = new DoacaoService(
                new DoacaoMongoRepository(MongoConfig.getDatabase()), instituicaoRepository);
        DoacaoController controller = new DoacaoController(service);

        InstituicaoService instituicaoService = new InstituicaoService(instituicaoRepository);
        InstituicaoController instituicaoController = new InstituicaoController(instituicaoService);

        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(mapper, false));
            config.staticFiles.add("/public", Location.CLASSPATH);
        });

        app.post("/doacoes", controller::registrar);
        app.get("/doacoes", controller::listar);
        app.get("/doacoes/{id}", controller::buscarPorId);
        app.patch("/doacoes/{id}/status", controller::atualizarStatus);
        app.delete("/doacoes/{id}", controller::remover);

        app.post("/instituicoes", instituicaoController::registrar);
        app.get("/instituicoes", instituicaoController::listar);
        app.get("/instituicoes/{id}", instituicaoController::buscarPorId);
        app.put("/instituicoes/{id}", instituicaoController::atualizar);
        app.delete("/instituicoes/{id}", instituicaoController::remover);
        app.get("/instituicoes/{id}/doacoes", controller::listarPorInstituicao);

        app.exception(DoacaoInvalidaException.class,
                (e, ctx) -> ctx.status(400).json(Map.of("erro", e.getMessage())));
        app.exception(DoacaoNaoEncontradaException.class,
                (e, ctx) -> ctx.status(404).json(Map.of("erro", e.getMessage())));
        app.exception(InstituicaoInvalidaException.class,
                (e, ctx) -> ctx.status(400).json(Map.of("erro", e.getMessage())));
        app.exception(InstituicaoNaoEncontradaException.class,
                (e, ctx) -> ctx.status(404).json(Map.of("erro", e.getMessage())));

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        app.start(port);
        System.out.println("API em http://localhost:" + port + "/doacoes e /instituicoes");
        System.out.println("Swagger UI em http://localhost:" + port + "/swagger-ui.html");
    }
}
