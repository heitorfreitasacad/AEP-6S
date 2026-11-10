package br.com.unicesumar.aep;

import br.com.unicesumar.aep.config.MongoConfig;
import br.com.unicesumar.aep.exception.DoacaoInvalidaException;
import br.com.unicesumar.aep.exception.DoacaoNaoEncontradaException;
import br.com.unicesumar.aep.exception.InstituicaoInvalidaException;
import br.com.unicesumar.aep.exception.InstituicaoNaoEncontradaException;
import br.com.unicesumar.aep.model.Contato;
import br.com.unicesumar.aep.model.Doacao;
import br.com.unicesumar.aep.model.Endereco;
import br.com.unicesumar.aep.model.Instituicao;
import br.com.unicesumar.aep.model.StatusDoacao;
import br.com.unicesumar.aep.repository.DoacaoMongoRepository;
import br.com.unicesumar.aep.repository.InstituicaoMongoRepository;
import br.com.unicesumar.aep.service.DoacaoService;
import br.com.unicesumar.aep.service.InstituicaoService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        InstituicaoMongoRepository instituicaoRepository = new InstituicaoMongoRepository(MongoConfig.getDatabase());
        DoacaoService service = new DoacaoService(
                new DoacaoMongoRepository(MongoConfig.getDatabase()), instituicaoRepository);
        InstituicaoService instituicaoService = new InstituicaoService(instituicaoRepository);
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== AEP - Sistema de Gestao de Doacoes de Alimentos ===");

        boolean continuar = true;
        while (continuar) {
            exibirMenu();
            String opcao = scanner.nextLine().trim();
            switch (opcao) {
                case "1":
                    registrarDoacao(scanner, service, instituicaoService);
                    break;
                case "2":
                    listarDoacoes(service);
                    break;
                case "3":
                    atualizarStatus(scanner, service);
                    break;
                case "4":
                    removerDoacao(scanner, service);
                    break;
                case "5":
                    cadastrarInstituicao(scanner, instituicaoService);
                    break;
                case "6":
                    listarInstituicoes(instituicaoService);
                    break;
                case "7":
                    atualizarInstituicao(scanner, instituicaoService);
                    break;
                case "8":
                    removerInstituicao(scanner, instituicaoService);
                    break;
                case "9":
                    listarDoacoesDaInstituicao(scanner, service);
                    break;
                case "0":
                    continuar = false;
                    break;
                default:
                    System.out.println("Opcao invalida.");
            }
        }

        System.out.println("Encerrado.");
    }

    private static void exibirMenu() {
        System.out.println("\n1) Registrar doacao");
        System.out.println("2) Listar doacoes");
        System.out.println("3) Atualizar status de uma doacao");
        System.out.println("4) Remover doacao");
        System.out.println("5) Cadastrar instituicao");
        System.out.println("6) Listar instituicoes");
        System.out.println("7) Atualizar instituicao");
        System.out.println("8) Remover instituicao");
        System.out.println("9) Listar doacoes de uma instituicao");
        System.out.println("0) Sair");
        System.out.print("Escolha uma opcao: ");
    }

    private static void registrarDoacao(Scanner scanner, DoacaoService service,
                                        InstituicaoService instituicaoService) {
        try {
            System.out.print("Doador: ");
            String doador = scanner.nextLine().trim();
            System.out.print("Item: ");
            String item = scanner.nextLine().trim();
            System.out.print("Quantidade: ");
            double quantidade = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Unidade (ex: kg, un): ");
            String unidade = scanner.nextLine().trim();
            System.out.print("Data da doacao (AAAA-MM-DD): ");
            LocalDate data = LocalDate.parse(scanner.nextLine().trim());
            listarInstituicoes(instituicaoService);
            System.out.print("Id da instituicao destinataria: ");
            String instituicaoId = scanner.nextLine().trim();

            Doacao doacao = service.registrar(doador, item, quantidade, unidade, data, instituicaoId);
            System.out.println("Doacao registrada com sucesso: " + doacao);
        } catch (NumberFormatException e) {
            System.out.println("Quantidade invalida.");
        } catch (DateTimeParseException e) {
            System.out.println("Data invalida. Use o formato AAAA-MM-DD.");
        } catch (DoacaoInvalidaException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void listarDoacoes(DoacaoService service) {
        List<Doacao> doacoes = service.listar();
        if (doacoes.isEmpty()) {
            System.out.println("Nenhuma doacao registrada.");
            return;
        }
        doacoes.forEach(System.out::println);
    }

    private static void atualizarStatus(Scanner scanner, DoacaoService service) {
        try {
            System.out.print("Id da doacao: ");
            String id = scanner.nextLine().trim();
            System.out.print("Novo status (PENDENTE, RECEBIDA, DISTRIBUIDA, CANCELADA): ");
            StatusDoacao status = StatusDoacao.valueOf(scanner.nextLine().trim().toUpperCase());

            service.atualizarStatus(id, status);
            System.out.println("Status atualizado com sucesso.");
        } catch (IllegalArgumentException e) {
            System.out.println("Status invalido.");
        } catch (DoacaoNaoEncontradaException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void removerDoacao(Scanner scanner, DoacaoService service) {
        try {
            System.out.print("Id da doacao: ");
            String id = scanner.nextLine().trim();
            service.remover(id);
            System.out.println("Doacao removida com sucesso.");
        } catch (DoacaoNaoEncontradaException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void cadastrarInstituicao(Scanner scanner, InstituicaoService service) {
        try {
            String nome = lerTexto(scanner, "Nome da instituicao: ");
            Endereco endereco = lerEndereco(scanner);
            List<Contato> contatos = lerContatos(scanner);

            Instituicao instituicao = service.registrar(nome, endereco, contatos);
            System.out.println("Instituicao cadastrada com sucesso: " + instituicao);
        } catch (InstituicaoInvalidaException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void listarInstituicoes(InstituicaoService service) {
        List<Instituicao> instituicoes = service.listar();
        if (instituicoes.isEmpty()) {
            System.out.println("Nenhuma instituicao cadastrada.");
            return;
        }
        instituicoes.forEach(System.out::println);
    }

    private static void atualizarInstituicao(Scanner scanner, InstituicaoService service) {
        try {
            String id = lerTexto(scanner, "Id da instituicao: ");
            String nome = lerTexto(scanner, "Novo nome: ");
            Endereco endereco = lerEndereco(scanner);
            List<Contato> contatos = lerContatos(scanner);

            Instituicao instituicao = service.atualizar(id, nome, endereco, contatos);
            System.out.println("Instituicao atualizada com sucesso: " + instituicao);
        } catch (InstituicaoInvalidaException | InstituicaoNaoEncontradaException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void removerInstituicao(Scanner scanner, InstituicaoService service) {
        try {
            service.remover(lerTexto(scanner, "Id da instituicao: "));
            System.out.println("Instituicao removida com sucesso.");
        } catch (InstituicaoNaoEncontradaException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void listarDoacoesDaInstituicao(Scanner scanner, DoacaoService service) {
        try {
            List<Doacao> doacoes = service.listarPorInstituicao(lerTexto(scanner, "Id da instituicao: "));
            if (doacoes.isEmpty()) {
                System.out.println("Nenhuma doacao para esta instituicao.");
                return;
            }
            doacoes.forEach(System.out::println);
        } catch (InstituicaoNaoEncontradaException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static Endereco lerEndereco(Scanner scanner) {
        String rua = lerTexto(scanner, "Rua: ");
        String cidade = lerTexto(scanner, "Cidade: ");
        String estado = lerTexto(scanner, "Estado (UF): ");
        return new Endereco(rua, cidade, estado);
    }

    private static List<Contato> lerContatos(Scanner scanner) {
        List<Contato> contatos = new ArrayList<>();
        while (lerTexto(scanner, "Adicionar contato? (s/n): ").equalsIgnoreCase("s")) {
            String nome = lerTexto(scanner, "  Nome do contato: ");
            String telefone = lerTexto(scanner, "  Telefone: ");
            String email = lerTexto(scanner, "  E-mail: ");
            contatos.add(new Contato(nome, telefone, email));
        }
        return contatos;
    }

    private static String lerTexto(Scanner scanner, String rotulo) {
        System.out.print(rotulo);
        return scanner.nextLine().trim();
    }
}
