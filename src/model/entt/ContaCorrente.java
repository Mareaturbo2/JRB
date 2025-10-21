package model.entt;

import model.excp.DomainException;

public class ContaCorrente extends Account {
    private double limiteChequeEspecial = 500.0;

    public ContaCorrente(Integer numero, String titular, String senha, Double saldo) {
        super(numero, titular, senha, saldo);
    }

    @Override
    public void sacar(Double valor) {
        if (valor == null || valor <= 0)
            throw new DomainException("Valor inválido para saque.");

        double limiteDisponivel = saldo + limiteChequeEspecial;
        if (valor > limiteDisponivel)
            throw new DomainException("Saldo insuficiente (limite de cheque especial excedido).");

        saldo -= valor;
        registrar("Saque (Conta Corrente)", -valor);
    }

    public double getLimiteChequeEspecial() {
        return limiteChequeEspecial;
    }

    public void setLimiteChequeEspecial(double limiteChequeEspecial) {
        this.limiteChequeEspecial = limiteChequeEspecial;
    }

    public double getValorUsadoChequeEspecial() {
        return saldo < 0 ? Math.abs(saldo) : 0.0;
    }

    @Override
    public String getTipoConta() {
        return "Conta Corrente";
    }
}
