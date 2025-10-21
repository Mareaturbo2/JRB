package view;

import java.util.Scanner;
import model.entt.Account;
import service.BankService;

public class MenuPrincipalView {
    private final Scanner sc = new Scanner(System.in);
    private final BankService bank;

    public MenuPrincipalView(BankService bank) {
        this.bank = bank;
    }

    public void exibir() {
        int opcao = -1;
        while (opcao != 0) {
            System.out.println("\n===== MENU BANCO DIGITAL =====");
            System.out.println("1 - Criar conta");
            System.out.println("2 - Acessar conta");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");
            try {
                opcao = Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1 -> new CriarContaView(bank).exibir();
                case 2 -> acessarConta();
                case 0 -> System.out.println("Encerrando...");
                default -> System.out.println("Opção inválida.");
            }
        }
    }
private void acessarConta() {
    System.out.println("\n=== ACESSO À CONTA ===");
    System.out.print("CPF: ");
    String cpf = sc.nextLine();

    Account conta = bank.buscarConta(cpf);
    if (conta == null) {
        System.out.println("Conta não encontrada.");
        return;
    }

    if (conta.isEncerrada()) {
        System.out.println("Conta encerrada.");
        return;
    }

    if (conta.estaBloqueada()) {
        System.out.println("Conta temporariamente bloqueada.");
        System.out.print("Deseja tentar desbloquear informando a senha? (s/n): ");
        String tentar = sc.nextLine().trim();
        if (!tentar.equalsIgnoreCase("s")) {
            return;
        }
        System.out.print("Senha: ");
        String senhaTentativa = sc.nextLine();
        boolean senhaCorreta = bank.validarSenha(conta, senhaTentativa);
        if (senhaCorreta) {
            conta.desbloquear();
            bank.salvar();
            System.out.println("Senha correta. Conta desbloqueada. Login realizado com sucesso!");
            new MenuContaView(bank, cpf, conta).exibir();
            return;
        } else {
            conta.registrarTentativaSenha(false);
            bank.salvar();
            if (conta.estaBloqueada()) {
                System.out.println("Senha incorreta. Conta novamente bloqueada.");
            } else {
                System.out.println("Senha incorreta.");
            }
            return;
        }
    }

    System.out.print("Senha: ");
    String senha = sc.nextLine();

    boolean senhaCorreta = bank.validarSenha(conta, senha);
    conta.registrarTentativaSenha(senhaCorreta);
    bank.salvar();

    if (!senhaCorreta) {
        System.out.println("Senha incorreta.");
        if (conta.estaBloqueada()) {
            System.out.println("Conta temporariamente bloqueada. Tente novamente mais tarde.");
        }
        return;
    }

    System.out.println("\nLogin realizado com sucesso!");
    new MenuContaView(bank, cpf, conta).exibir();
}

}
