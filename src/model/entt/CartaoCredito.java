package model.entt;

import java.time.LocalDate;
import model.excp.DomainException;
import service.NumberGenerator;

public class CartaoCredito {
    private String numero;
    private String validade;
    private String cvv;
    private double limite;
    private Fatura faturaAtual;

    public CartaoCredito(double limite) {
        this.numero = NumberGenerator.gerarNumeroCartao();
        this.validade = LocalDate.now().plusYears(5)
                .format(java.time.format.DateTimeFormatter.ofPattern("MM/yy"));
        this.cvv = NumberGenerator.gerarCVV();
        this.limite = limite;
        this.faturaAtual = new Fatura();
    }

    public void comprar(String descricao, double valor) {
        if (valor <= 0) throw new DomainException("Valor inválido para compra.");
        if (valor > getLimiteDisponivel()) throw new DomainException("Limite insuficiente.");
        faturaAtual.adicionarCompra(descricao, valor);
    }

    public void pagarFatura(Account conta) {
        if (faturaAtual.getTotal() <= 0) throw new DomainException("Não há fatura em aberto.");
        if (faturaAtual.getTotal() > conta.getSaldo())
            throw new DomainException("Saldo insuficiente para pagar a fatura.");
        conta.debitarInterno(faturaAtual.getTotal(), "Pagamento de Fatura do Cartão");
        faturaAtual.pagar();
    }

    public double getLimite() { return limite; }
    public double getFaturaTotal() { return faturaAtual.getTotal(); }
    public Fatura getFaturaAtual() { return faturaAtual; }
    public double getLimiteDisponivel() { return limite - faturaAtual.getTotal(); }
    public String getNumero() { return numero; }
    public String getValidade() { return validade; }
    public String getCvv() { return cvv; }
    public CartaoCredito(String numero, String validade, String cvv, double limite, Fatura fatura) {
    this.numero = numero;
    this.validade = validade;
    this.cvv = cvv;
    this.limite = limite;
    this.faturaAtual = fatura != null ? fatura : new Fatura();
}
}
