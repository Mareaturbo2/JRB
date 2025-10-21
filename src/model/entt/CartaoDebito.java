package model.entt;

import java.time.LocalDate;
import model.excp.DomainException;
import service.NumberGenerator;

public class CartaoDebito {
    private String numero;
    private String validade;
    private String cvv;

    public CartaoDebito() {
        this.numero = NumberGenerator.gerarNumeroCartao();
        this.validade = LocalDate.now().plusYears(5)
                .format(java.time.format.DateTimeFormatter.ofPattern("MM/yy"));
        this.cvv = NumberGenerator.gerarCVV();
    }

    public void comprar(Account conta, double valor, String descricao) {
        if (valor <= 0) throw new DomainException("Valor inválido para compra.");
        if (valor > conta.getSaldo()) throw new DomainException("Saldo insuficiente para realizar a compra.");
        conta.debitarInterno(valor, "Compra Débito: " + descricao);
    }

    public String getNumero() { return numero; }
    public String getValidade() { return validade; }
    public String getCvv() { return cvv; }
    public CartaoDebito(String numero, String validade, String cvv) {
    this.numero = numero;
    this.validade = validade;
    this.cvv = cvv;
}
}
