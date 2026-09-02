package br.com.unicesumar.aep;

import br.com.unicesumar.aep.config.MongoConfig;
import br.com.unicesumar.aep.exception.DoacaoInvalidaException;
import br.com.unicesumar.aep.exception.DoacaoNaoEncontradaException;
import br.com.unicesumar.aep.model.Doacao;
import br.com.unicesumar.aep.model.StatusDoacao;
import br.com.unicesumar.aep.repository.DoacaoMongoRepository;
import br.com.unicesumar.aep.service.DoacaoService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        DoacaoService service = new DoacaoService(new DoacaoMongoRepository(MongoConfig.getDatabase()));
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== AEP - Sistema de Gestao de Doacoes de Alimentos ===");

        boolean continuar = true;
        while (continuar) {
            exibirMenu();
            String opcao = scanner.nextLine().trim();
            switch (opcao) {
                case "1":
                    registrarDoacao(scanner, service);
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
        System.out.println("0) Sair");
        System.out.print("Escolha uma opcao: ");
    }

    private static void registrarDoacao(Scanner scanner, DoacaoService service) {
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

            Doacao doacao = service.registrar(doador, item, quantidade, unidade, data);
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
}
