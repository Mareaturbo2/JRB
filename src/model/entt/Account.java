package model.entt;

import java.util.ArrayList;
import java.util.List;
import model.excp.DomainException;

public abstract class Account {
    protected Integer numero;
    protected String titular;
    protected String senha;
    protected Double saldo;
    protected boolean encerrada = false;
    protected String dataEncerramento = null;
    protected List<Movimentacao> movimentacoes = new ArrayList<>();

    //cartões vinculados
    protected CartaoCredito cartaoCredito;
    protected CartaoDebito cartaoDebito;

    //controle de login
    protected int tentativasSenha = 0;
    protected long bloqueioAte = 0L;

    public Account(Integer numero, String titular, String senha, Double saldo) {
        this.numero = numero;
        this.titular = titular;
        this.senha = senha;
        this.saldo = saldo != null ? saldo : 0.0;
    }

    //regras do banco
    public void depositar(Double valor) {
        if (valor == null || valor <= 0) throw new DomainException("Valor inválido para depósito.");
        saldo += valor;
        registrar("Depósito", valor);
    }

    public void sacar(Double valor) {
        if (valor == null || valor <= 0) throw new DomainException("Valor inválido para saque.");
        if (valor > saldo) throw new DomainException("Saldo insuficiente.");
        saldo -= valor;
        registrar("Saque", -valor);
    }

    public void debitarInterno(Double valor, String descricao) {
        if (valor == null || valor <= 0) throw new DomainException("Valor inválido.");
        if (valor > saldo) throw new DomainException("Saldo insuficiente.");
        saldo -= valor;
        registrar(descricao, -valor);
    }

    protected void registrar(String tipo, Double valor) {
        movimentacoes.add(Movimentacao.of(tipo, valor));
    }

    //login e bloqueio
    public boolean validarSenha(String senhaDigitada) {
        return senha.equals(senhaDigitada);
    }

    public boolean estaBloqueada() {
        return System.currentTimeMillis() < bloqueioAte;
    }

    public void registrarTentativaSenha(boolean sucesso) {
        if (sucesso) {
            tentativasSenha = 0;
            bloqueioAte = 0L;
        } else {
            tentativasSenha++;
            if (tentativasSenha >= 3) bloquearTemporariamente();
        }
    }

    private void bloquearTemporariamente() {
        tentativasSenha = 0;
        bloqueioAte = System.currentTimeMillis() + (5 * 60 * 1000L); // 5 minutos
    }

    public void desbloquear() {
        tentativasSenha = 0;
        bloqueioAte = 0L;
    }

    //GETTERS / SETTERS 
    public Integer getNumero() { return numero; }
    public String getTitular() { return titular; }
    public Double getSaldo() { return saldo; }
    public boolean isEncerrada() { return encerrada; }
    public List<Movimentacao> getMovimentacoes() { return movimentacoes; }
    public void setMovimentacoes(List<Movimentacao> movs) { this.movimentacoes = movs; }
    public String getSenhaMaskless() { return senha; }
    public void setSaldo(Double saldo) { this.saldo = saldo; }

    //cartão
    public CartaoCredito getCartaoCredito() { return cartaoCredito; }
    public void setCartaoCredito(CartaoCredito c) { this.cartaoCredito = c; }

    public CartaoDebito getCartaoDebito() { return cartaoDebito; }
    public void setCartaoDebito(CartaoDebito c) { this.cartaoDebito = c; }

    //cancelamento
    public void encerrar() {
    this.encerrada = true;
    this.dataEncerramento = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    registrar("Conta encerrada em " + this.dataEncerramento, 0.0);
}
public String getDataEncerramento() {
    return dataEncerramento;
}

    //tipo da conta
    public abstract String getTipoConta();
}
