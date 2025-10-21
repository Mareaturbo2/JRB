package view;

import java.util.Scanner;
import model.entt.Account;
import model.excp.DomainException;
import service.BankService;

public class CriarContaView {

    private final Scanner sc = new Scanner(System.in);
    private final BankService bank;

    public CriarContaView(BankService bank) {
        this.bank = bank;
    }

    public void exibir() {
        System.out.println("\n=== CRIAR CONTA ===");

        System.out.print("Nome do titular: ");
        String nome = sc.nextLine().trim();

        System.out.print("CPF: ");
        String cpf = sc.nextLine().trim();

        System.out.print("Senha: ");
        String senha = sc.nextLine().trim();

        System.out.println("Escolha o tipo de conta:");
        System.out.println("1 - Corrente");
        System.out.println("2 - Poupança");
        System.out.print("Opção: ");

        int tipoEscolhido;
        try {
            tipoEscolhido = Integer.parseInt(sc.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Tipo inválido. Operação cancelada.");
            return;
        }

        String tipo = (tipoEscolhido == 2) ? "poupanca" : "corrente";

        Double saldoInicial = 0.0;
        System.out.print("Deseja informar saldo inicial? (s/n): ");
        String resp = sc.nextLine().trim();
        if (resp.equalsIgnoreCase("s")) {
            System.out.print("Valor inicial: ");
            try {
                saldoInicial = Double.parseDouble(sc.nextLine().replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.println("Formato inválido. O saldo inicial será R$ 0.00.");
                saldoInicial = 0.0;
            }
        }

        try {
            Account conta = bank.criarConta(nome, cpf, senha, saldoInicial, tipo);
            System.out.println("\nConta criada com sucesso!");
            System.out.println("Número da conta: " + conta.getNumero());
            System.out.println("Titular: " + conta.getTitular());
            System.out.println("Tipo: " + conta.getTipoConta());
            System.out.printf("Saldo inicial: R$ %.2f%n", conta.getSaldo());
        } catch (DomainException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
