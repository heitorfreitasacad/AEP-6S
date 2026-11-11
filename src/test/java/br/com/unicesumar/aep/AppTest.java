package br.com.unicesumar.aep;

import br.com.unicesumar.aep.model.Contato;
import br.com.unicesumar.aep.model.Doacao;
import br.com.unicesumar.aep.model.Endereco;
import br.com.unicesumar.aep.model.Instituicao;
import br.com.unicesumar.aep.repository.DoacaoMongoRepository;
import br.com.unicesumar.aep.repository.InstituicaoMongoRepository;
import br.com.unicesumar.aep.service.DoacaoService;
import br.com.unicesumar.aep.service.InstituicaoService;
import br.com.unicesumar.aep.support.MongoEmMemoria;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercita a CLI com entrada simulada e servicos reais sobre um MongoDB em
 * memoria. Cada teste termina a entrada com a opcao 0 (sair).
 */
class AppTest {

    private static MongoEmMemoria mongo;

    private DoacaoService doacaoService;
    private InstituicaoService instituicaoService;

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
        MongoDatabase database = mongo.novoBanco();
        InstituicaoMongoRepository instituicaoRepository = new InstituicaoMongoRepository(database);
        instituicaoService = new InstituicaoService(instituicaoRepository);
        doacaoService = new DoacaoService(new DoacaoMongoRepository(database), instituicaoRepository);
    }

    private String rodar(String... linhas) {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));
        try {
            App.executar(doacaoService, instituicaoService, new Scanner(String.join("\n", linhas) + "\n"));
        } finally {
            System.setOut(original);
        }
        return saida.toString(StandardCharsets.UTF_8);
    }

    private Instituicao criarInstituicao() {
        return instituicaoService.registrar("Banco de Alimentos",
                new Endereco("Rua A", "Maringa", "PR"),
                List.of(new Contato("Ana", "44999990000", "ana@ong.org")));
    }

    private Doacao criarDoacao(String instituicaoId) {
        return doacaoService.registrar("Maria", "Arroz", 10, "kg", LocalDate.of(2026, 8, 20), instituicaoId);
    }

    @Test
    void deveMostrarMenuEEncerrar() {
        String saida = rodar("0");

        assertTrue(saida.contains("AEP - Sistema de Gestao de Doacoes de Alimentos"));
        assertTrue(saida.contains("9) Listar doacoes de uma instituicao"));
        assertTrue(saida.contains("Encerrado."));
    }

    @Test
    void deveAvisarOpcaoInvalida() {
        assertTrue(rodar("x", "0").contains("Opcao invalida."));
    }

    @Test
    void deveCadastrarInstituicaoComContatoELista() {
        String saida = rodar("5", "ONG CLI", "Rua X", "Maringa", "PR", "s", "Ana", "4499", "ana@x.org", "n", "6", "0");

        assertTrue(saida.contains("Instituicao cadastrada com sucesso"));
        assertEquals(1, instituicaoService.listar().size());
        assertEquals("Ana", instituicaoService.listar().get(0).getContatos().get(0).getNome());
        assertTrue(saida.contains("ONG CLI"));
    }

    @Test
    void deveMostrarErroAoCadastrarInstituicaoInvalida() {
        String saida = rodar("5", " ", "Rua X", "Maringa", "PR", "n", "0");

        assertTrue(saida.contains("Erro:"));
        assertTrue(instituicaoService.listar().isEmpty());
    }

    @Test
    void deveAvisarQuandoNaoHaInstituicoesNemDoacoes() {
        String saida = rodar("6", "2", "0");

        assertTrue(saida.contains("Nenhuma instituicao cadastrada."));
        assertTrue(saida.contains("Nenhuma doacao registrada."));
    }

    @Test
    void deveRegistrarDoacaoParaInstituicaoExistente() {
        Instituicao instituicao = criarInstituicao();

        String saida = rodar("1", "Maria", "Arroz", "10", "kg", "2026-08-20", instituicao.getId(), "2", "0");

        assertTrue(saida.contains("Doacao registrada com sucesso"));
        assertEquals(1, doacaoService.listar().size());
        assertEquals(instituicao.getId(), doacaoService.listar().get(0).getInstituicaoId());
    }

    @Test
    void deveRejeitarEntradasInvalidasAoRegistrarDoacao() {
        Instituicao instituicao = criarInstituicao();

        String quantidadeRuim = rodar("1", "Maria", "Arroz", "abc", "0");
        String dataRuim = rodar("1", "Maria", "Arroz", "10", "kg", "ontem", "0");
        String semInstituicao = rodar("1", "Maria", "Arroz", "10", "kg", "2026-08-20",
                new ObjectId().toHexString(), "0");

        assertTrue(quantidadeRuim.contains("Quantidade invalida."));
        assertTrue(dataRuim.contains("Data invalida."));
        assertTrue(semInstituicao.contains("Erro:"));
        assertTrue(doacaoService.listar().isEmpty());
        assertEquals(1, instituicaoService.listar().size());
        assertEquals(instituicao.getId(), instituicaoService.listar().get(0).getId());
    }

    @Test
    void deveAtualizarStatusDeDoacao() {
        Doacao doacao = criarDoacao(criarInstituicao().getId());

        String saida = rodar("3", doacao.getId(), "recebida", "2", "0");

        assertTrue(saida.contains("Status atualizado com sucesso."));
        assertTrue(saida.contains("RECEBIDA"));
    }

    @Test
    void deveRejeitarStatusInvalidoEDoacaoInexistente() {
        Doacao doacao = criarDoacao(criarInstituicao().getId());

        String statusRuim = rodar("3", doacao.getId(), "foo", "0");
        String inexistente = rodar("3", new ObjectId().toHexString(), "recebida", "0");

        assertTrue(statusRuim.contains("Status invalido."));
        assertTrue(inexistente.contains("Erro:"));
    }

    @Test
    void deveRemoverDoacaoEMostrarErroQuandoInexistente() {
        Doacao doacao = criarDoacao(criarInstituicao().getId());

        String removida = rodar("4", doacao.getId(), "0");
        String inexistente = rodar("4", doacao.getId(), "0");

        assertTrue(removida.contains("Doacao removida com sucesso."));
        assertTrue(inexistente.contains("Erro:"));
        assertTrue(doacaoService.listar().isEmpty());
    }

    @Test
    void deveAtualizarInstituicaoEMostrarErros() {
        Instituicao instituicao = criarInstituicao();

        String atualizada = rodar("7", instituicao.getId(), "Novo Nome", "Rua B", "Londrina", "PR", "n", "0");
        String inexistente = rodar("7", new ObjectId().toHexString(), "X", "R", "C", "PR", "n", "0");
        String invalida = rodar("7", instituicao.getId(), " ", "R", "C", "PR", "n", "0");

        assertTrue(atualizada.contains("Instituicao atualizada com sucesso"));
        assertEquals("Novo Nome", instituicaoService.buscarPorId(instituicao.getId()).getNome());
        assertTrue(inexistente.contains("Erro:"));
        assertTrue(invalida.contains("Erro:"));
    }

    @Test
    void deveRemoverInstituicaoEMostrarErroQuandoInexistente() {
        Instituicao instituicao = criarInstituicao();

        String removida = rodar("8", instituicao.getId(), "0");
        String inexistente = rodar("8", instituicao.getId(), "0");

        assertTrue(removida.contains("Instituicao removida com sucesso."));
        assertTrue(inexistente.contains("Erro:"));
    }

    @Test
    void deveListarDoacoesDeUmaInstituicao() {
        Instituicao comDoacao = criarInstituicao();
        criarDoacao(comDoacao.getId());
        Instituicao semDoacao = instituicaoService.registrar("Vazia", new Endereco("R", "C", "PR"), List.of());

        String cheia = rodar("9", comDoacao.getId(), "0");
        String vazia = rodar("9", semDoacao.getId(), "0");
        String inexistente = rodar("9", new ObjectId().toHexString(), "0");

        assertTrue(cheia.contains("Maria"));
        assertTrue(vazia.contains("Nenhuma doacao para esta instituicao."));
        assertTrue(inexistente.contains("Erro:"));
    }
}
