package view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import model.entt.Account;
import model.entt.ContaPoupanca;
import model.entt.Fatura;
import model.excp.DomainException;
import service.BankService;
import service.CartaoService;
import service.ExtratoService;

public class MenuContaView {

    private final Scanner sc = new Scanner(System.in);
    private final BankService bank;
    private final String cpf;
    private final Account conta;
    private final ExtratoService extrato = new ExtratoService();

    public MenuContaView(BankService bank, String cpf, Account conta) {
        this.bank = bank;
        this.cpf = cpf;
        this.conta = conta;
    }

    public void exibir() {
        int opcao = -1;
        while (opcao != 0) {
            System.out.println("\n=== MENU DA CONTA ===");
            System.out.println("1 - Consultar Saldo");
            System.out.println("2 - Depositar");
            System.out.println("3 - Sacar");
            System.out.println("4 - Extrato (Completo/Período + PDF)");
            System.out.println("5 - Encerrar Conta");
            System.out.println("6 - Pagar Conta/Boleto");
            System.out.println("7 - Transferência Entre Contas");
            System.out.println("8 - Solicitar Cartão de Crédito");
            System.out.println("9 - Solicitar Cartão de Débito");
            System.out.println("10 - Compra com Cartão de Débito");
            System.out.println("11 - Compra com Cartão de Crédito");
            System.out.println("12 - Ver Fatura do Cartão de Crédito");
            System.out.println("13 - Pagar Fatura do Cartão de Crédito");
            System.out.println("14 - Investir na Poupança (com rendimento)");
            System.out.println("15 - Resgatar da Poupança");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");

            try {
                opcao = Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e) {
                opcao = -1;
            }

            try {
                switch (opcao) {
                    case 1 -> consultarSaldo();
                    case 2 -> depositar();
                    case 3 -> sacar();
                    case 4 -> extrato();
                    case 5 -> encerrarConta();
                    case 6 -> pagarBoleto();
                    case 7 -> transferir();
                    case 8 -> solicitarCartaoCredito();
                    case 9 -> solicitarCartaoDebito();
                    case 10 -> comprarDebito();
                    case 11 -> comprarCredito();
                    case 12 -> verFatura();
                    case 13 -> pagarFatura();
                    case 14 -> investirPoupanca();
                    case 15 -> resgatarPoupanca();
                    case 0 -> System.out.println("Saindo da conta...");
                    default -> System.out.println("Opção inválida.");
                }
            } catch (DomainException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

   //consulta saldo
    private void consultarSaldo() {
    System.out.println("\n=== SALDO DA CONTA ===");

    //exibe saldo padrão
    System.out.printf("Saldo atual: R$ %.2f%n", conta.getSaldo());

    //se for conta corrente, mostra cheque especial detalhado
    if (conta instanceof model.entt.ContaCorrente cc) {
        double limiteTotal = cc.getLimiteChequeEspecial();
        double limiteUsado = cc.getValorUsadoChequeEspecial();
        double limiteDisponivel = limiteTotal - limiteUsado;

        System.out.printf("Limite de Cheque Especial: R$ %.2f%n", limiteTotal);

        if (cc.getSaldo() < 0) {
            System.out.printf("⚠️  Valor usado do cheque especial: R$ %.2f%n", limiteUsado);
        }

        System.out.printf("💳 Limite disponível: R$ %.2f%n", limiteDisponivel);
    }

    //se for poupança, mostra investimento
    if (conta instanceof model.entt.ContaPoupanca cp) {
        System.out.printf("Investimento em Poupança: R$ %.2f%n", cp.getInvestimento());
    }

    //se estiver encerrada
    if (conta.isEncerrada()) {
        System.out.println("⚠️ Conta encerrada em: " + conta.getDataEncerramento());
    }
}

    //deposito
    private void depositar() {
        System.out.print("Valor do depósito: ");
        try {
            double v = Double.parseDouble(sc.nextLine().replace(",", ".").trim());
            bank.depositar(cpf, v);
            System.out.printf("Depósito realizado! Saldo atual: R$ %.2f%n", conta.getSaldo());
        } catch (NumberFormatException ex) {
            System.out.println("Formato inválido de valor.");
        }
    }

   //saque
    private void sacar() {
        System.out.print("Valor do saque: ");
        try {
            double v = Double.parseDouble(sc.nextLine().replace(",", ".").trim());
            bank.sacar(cpf, v);
            System.out.printf("Saque realizado! Saldo atual: R$ %.2f%n", conta.getSaldo());
        } catch (NumberFormatException ex) {
            System.out.println("Formato inválido de valor.");
        }
    }

    //extrato
    private void extrato() {
        if (conta.getMovimentacoes().isEmpty()) {
            System.out.println("Nenhuma movimentação encontrada.");
            return;
        }

        System.out.print("Deseja filtrar por período? (s/n): ");
        String op = sc.nextLine().trim();
        if (op.equalsIgnoreCase("s")) {
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                System.out.print("Data inicial (dd/MM/yyyy): ");
                LocalDate ini = LocalDate.parse(sc.nextLine(), fmt);
                System.out.print("Data final (dd/MM/yyyy): ");
                LocalDate fim = LocalDate.parse(sc.nextLine(), fmt);
                if (ini.isAfter(fim)) {
                    System.out.println("Intervalo de datas inválido.");
                    return;
                }
                extrato.mostrarFiltrado(conta, ini, fim);
            } catch (Exception e) {
                System.out.println("Formato de data inválido.");
                return;
            }
        } else {
            extrato.mostrarCompleto(conta);
        }

        System.out.print("\nDeseja exportar o extrato em PDF? (s/n): ");
        String exp = sc.nextLine().trim();
        if (exp.equalsIgnoreCase("s")) {
            extrato.exportarExtratoPDF(conta);
        }
    }

 //cancela a conta
    private void encerrarConta() {
        if (conta.getSaldo() > 0) {
            System.out.println("Conta não pode ser encerrada com saldo disponível.");
            return;
        }
        System.out.print("Tem certeza que deseja encerrar sua conta? (s/n): ");
        String confirm = sc.nextLine();
        if (confirm.equalsIgnoreCase("s")) {
            bank.encerrarConta(cpf);
            System.out.println("Conta encerrada com sucesso.");
            System.exit(0);
        } else {
            System.out.println("Operação cancelada.");
        }
    }

    //pagamento de boleto
    private void pagarBoleto() {
        System.out.println("\n=== PAGAMENTO DE CONTA/BOLETO ===");
        System.out.print("Código do boleto: ");
        String codigo = sc.nextLine().trim();

        System.out.print("Valor do pagamento: ");
        double valor;
        try {
            valor = Double.parseDouble(sc.nextLine().replace(",", "."));
        } catch (NumberFormatException e) {
            System.out.println("Formato inválido de valor.");
            return;
        }

        if (valor <= 0) {
            System.out.println("Valor inválido para pagamento.");
            return;
        }

        System.out.print("Data de vencimento (opcional, formato dd/MM/yyyy): ");
        String dataVenc = sc.nextLine().trim();

        try {
            bank.pagarBoleto(cpf, codigo, valor, dataVenc);
            System.out.printf("Pagamento de boleto realizado com sucesso! Valor: R$ %.2f%n", valor);
            System.out.printf("Saldo atual: R$ %.2f%n", conta.getSaldo());
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

   //transferencia
    private void transferir() {
        System.out.println("\n=== TRANSFERÊNCIA ENTRE CONTAS ===");
        System.out.print("CPF da conta destino: ");
        String cpfDestino = sc.nextLine().trim();

        if (cpfDestino.equals(cpf)) {
            System.out.println("Não é possível transferir para a própria conta.");
            return;
        }

        var contaDestino = bank.buscarConta(cpfDestino);
        if (contaDestino == null) {
            System.out.println("Conta de destino não encontrada.");
            return;
        }
        if (contaDestino.isEncerrada()) {
            System.out.println("Conta de destino encerrada.");
            return;
        }

        System.out.print("Valor da transferência: ");
        double valor;
        try {
            valor = Double.parseDouble(sc.nextLine().replace(",", "."));
        } catch (NumberFormatException e) {
            System.out.println("Formato inválido de valor.");
            return;
        }

        if (valor <= 0) {
            System.out.println("Valor inválido para transferência.");
            return;
        }

        try {
            bank.transferir(cpf, cpfDestino, valor);
            System.out.printf("Transferência realizada com sucesso! Valor: R$ %.2f%n", valor);
            System.out.printf("Saldo atual: R$ %.2f%n", conta.getSaldo());
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    //cartão debito e credito
    private void solicitarCartaoCredito() {
        System.out.println("\n=== SOLICITAR CARTÃO DE CRÉDITO ===");
        if (conta.getCartaoCredito() != null) {
            System.out.println("Você já possui um cartão de crédito!");
            return;
        }

        System.out.print("Informe o limite desejado: ");
        try {
            double limite = Double.parseDouble(sc.nextLine().replace(",", "."));
            CartaoService cs = new CartaoService(bank);
            cs.solicitarCartaoCredito(cpf, limite);
            System.out.println("Cartão de crédito criado com sucesso!");
            System.out.println("Número: " + conta.getCartaoCredito().getNumero());
            System.out.println("Validade: " + conta.getCartaoCredito().getValidade());
            System.out.println("CVV: " + conta.getCartaoCredito().getCvv());
            System.out.printf("Limite: R$ %.2f%n", conta.getCartaoCredito().getLimite());
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void solicitarCartaoDebito() {
        System.out.println("\n=== SOLICITAR CARTÃO DE DÉBITO ===");
        if (conta.getCartaoDebito() != null) {
            System.out.println("Você já possui um cartão de débito!");
            return;
        }
        try {
            CartaoService cs = new CartaoService(bank);
            cs.solicitarCartaoDebito(cpf);
            System.out.println("Cartão de débito criado com sucesso!");
            System.out.println("Número: " + conta.getCartaoDebito().getNumero());
            System.out.println("Validade: " + conta.getCartaoDebito().getValidade());
            System.out.println("CVV: " + conta.getCartaoDebito().getCvv());
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void comprarDebito() {
        System.out.println("\n=== COMPRA COM CARTÃO DE DÉBITO ===");
        if (conta.getCartaoDebito() == null) {
            System.out.println("Você ainda não possui um cartão de débito.");
            return;
        }
        System.out.print("Descrição da compra: ");
        String descricao = sc.nextLine().trim();

        System.out.print("Valor da compra: ");
        double valor;
        try {
            valor = Double.parseDouble(sc.nextLine().replace(",", "."));
        } catch (NumberFormatException e) {
            System.out.println("Formato inválido de valor.");
            return;
        }

        try {
            CartaoService cs = new CartaoService(bank);
            cs.comprarDebito(cpf, valor, descricao);
            System.out.printf("Compra aprovada! Valor: R$ %.2f%n", valor);
            System.out.printf("Saldo atual: R$ %.2f%n", conta.getSaldo());
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void comprarCredito() {
        System.out.println("\n=== COMPRA COM CARTÃO DE CRÉDITO ===");
        if (conta.getCartaoCredito() == null) {
            System.out.println("Você ainda não possui um cartão de crédito.");
            return;
        }

        System.out.print("Descrição da compra: ");
        String desc = sc.nextLine().trim();
        System.out.print("Valor da compra: ");
        double valor;
        try {
            valor = Double.parseDouble(sc.nextLine().replace(",", "."));
        } catch (NumberFormatException e) {
            System.out.println("Formato inválido de valor.");
            return;
        }

        try {
            CartaoService cs = new CartaoService(bank);
            cs.comprarCredito(cpf, valor, desc);
            System.out.printf("Compra realizada! Valor: R$ %.2f%n", valor);
            System.out.printf("Limite disponível: R$ %.2f%n", conta.getCartaoCredito().getLimiteDisponivel());
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void verFatura() {
        System.out.println("\n=== FATURA ATUAL ===");
        if (conta.getCartaoCredito() == null) {
            System.out.println("Você ainda não possui um cartão de crédito.");
            return;
        }

        Fatura fatura = conta.getCartaoCredito().getFaturaAtual();
        if (fatura.getCompras().isEmpty()) {
            System.out.println("Nenhuma compra lançada na fatura.");
            return;
        }

        System.out.println(fatura.toString());
        fatura.getCompras().forEach(m ->
                System.out.printf("%s | %s de R$ %.2f%n", m.getDataHora(), m.getTipo(), m.getValor())
        );
    }

    private void pagarFatura() {
        System.out.println("\n=== PAGAMENTO DE FATURA ===");
        if (conta.getCartaoCredito() == null) {
            System.out.println("Você ainda não possui um cartão de crédito.");
            return;
        }

        try {
            double total = conta.getCartaoCredito().getFaturaTotal();
            if (total <= 0) {
                System.out.println("Não há fatura em aberto.");
                return;
            }
            System.out.printf("Valor da fatura: R$ %.2f%n", total);
            System.out.print("Confirmar pagamento? (s/n): ");
            String conf = sc.nextLine().trim();
            if (conf.equalsIgnoreCase("s")) {
                CartaoService cs = new CartaoService(bank);
                cs.pagarFatura(cpf);
                System.out.println("Fatura paga com sucesso!");
                System.out.printf("Saldo atual: R$ %.2f%n", conta.getSaldo());
            } else {
                System.out.println("Pagamento cancelado.");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

   //rendimento da poupança
    //investir na Poupança
        private void investirPoupanca() {
            if (!(conta instanceof ContaPoupanca cp)) {
                System.out.println("Essa função é apenas para contas poupança.");
                return;
            }

            System.out.print("Valor para investir (será debitado do saldo): ");
            double valor;
            try {
                valor = Double.parseDouble(sc.nextLine().replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.println("Formato inválido de valor.");
                return;
            }

            try {
                cp.investir(valor);
                bank.salvar();
                System.out.printf("Investimento realizado! Valor aplicado: R$ %.2f%n", valor);
                System.out.printf("Rendimento imediato: R$ %.2f%n", valor * 0.005);
                System.out.printf("Total investido agora: R$ %.2f | Saldo disponível: R$ %.2f%n",
                        cp.getInvestimento(), conta.getSaldo());
            } catch (DomainException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }

        //resgatar da Poupança
        private void resgatarPoupanca() {
            if (!(conta instanceof ContaPoupanca cp)) {
                System.out.println("Essa função é apenas para contas poupança.");
                return;
            }

            System.out.print("Valor para resgatar (voltará ao saldo): ");
            double valor;
            try {
                valor = Double.parseDouble(sc.nextLine().replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.println("Formato inválido de valor.");
                return;
            }

            try {
                cp.resgatar(valor);
                bank.salvar();
                System.out.printf("Resgate concluído! Investido agora: R$ %.2f | Saldo disponível: R$ %.2f%n",
                        cp.getInvestimento(), conta.getSaldo());
            } catch (DomainException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }

}