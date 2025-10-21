package service;

import model.entt.*;
import model.excp.DomainException;


public class CartaoService {
    private final BankService bank;

    public CartaoService(BankService bank) {
        this.bank = bank;
    }
    

    public void solicitarCartaoCredito(String cpf, double limite) {
        Account conta = bank.buscarConta(cpf);
        if (conta == null) throw new DomainException("Conta não encontrada.");
        // 🚫 Bloqueio para contas poupança
                if (conta instanceof model.entt.ContaPoupanca) {
                    throw new RuntimeException("Contas poupança não podem possuir cartão de crédito.");
                }
        if (conta.isEncerrada()) throw new DomainException("Conta encerrada.");
        if (conta.getCartaoCredito() != null) throw new DomainException("Conta já possui cartão de crédito.");
        if (limite <= 0) throw new DomainException("Limite inválido.");

        CartaoCredito novo = new CartaoCredito(limite);
        conta.setCartaoCredito(novo);
        bank.salvar();
    }

    public void solicitarCartaoDebito(String cpf) {
        Account conta = bank.buscarConta(cpf);
        if (conta == null) throw new DomainException("Conta não encontrada.");
        if (conta.isEncerrada()) throw new DomainException("Conta encerrada.");
        if (conta.getCartaoDebito() != null) throw new DomainException("Conta já possui cartão de débito.");

        CartaoDebito novo = new CartaoDebito();
        conta.setCartaoDebito(novo);
        bank.salvar();
    }

    public void comprarDebito(String cpf, double valor, String descricao) {
        Account conta = bank.buscarConta(cpf);
        if (conta == null) throw new DomainException("Conta não encontrada.");
        if (conta.isEncerrada()) throw new DomainException("Conta encerrada.");
        if (conta.getCartaoDebito() == null) throw new DomainException("Conta não possui cartão de débito.");

        conta.getCartaoDebito().comprar(conta, valor, descricao);
        bank.salvar();
    }

    public void comprarCredito(String cpf, double valor, String descricao) {
        Account conta = bank.buscarConta(cpf);
        if (conta == null) throw new DomainException("Conta não encontrada.");
        if (conta.isEncerrada()) throw new DomainException("Conta encerrada.");
        if (conta.getCartaoCredito() == null) throw new DomainException("Conta não possui cartão de crédito.");

        conta.getCartaoCredito().comprar(descricao, valor);
        bank.salvar();
    }
    public CartaoDebito obterCartaoDebito(String cpf) throws Exception {
        Account conta = bank.buscarConta(cpf);
        if (conta == null) throw new Exception("Conta não encontrada.");
        if (conta.getCartaoDebito() == null) throw new Exception("Cartão de débito não encontrado.");
        return conta.getCartaoDebito();
    }



    public void pagarFatura(String cpf) {
        Account conta = bank.buscarConta(cpf);
        if (conta == null) throw new DomainException("Conta não encontrada.");
        if (conta.isEncerrada()) throw new DomainException("Conta encerrada.");
        if (conta.getCartaoCredito() == null) throw new DomainException("Conta não possui cartão de crédito.");

        conta.getCartaoCredito().pagarFatura(conta);
        bank.salvar();
    }
}