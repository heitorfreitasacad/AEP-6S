package br.com.unicesumar.aep;

import java.util.Scanner;

/**
 * So o menu por enquanto; CRUD entra via
 * service/controller nos futuros commits.
 */
public class App {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== AEP - Sistema de Gestao de Doacoes de Alimentos ===");

        boolean continuar = true;
        while (continuar) {
            exibirMenu();
            String opcao = scanner.nextLine().trim();
            switch (opcao) {
                case "1":;
                case "2":;
                case "3":;
                case "4":;
                case "0": continuar = false;
                default: System.out.println("Opcao invalida.");
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
}
