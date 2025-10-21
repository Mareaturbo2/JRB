package model.entt;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Fatura {
    private String mesReferencia;
    private LocalDate dataFechamento;
    private LocalDate dataVencimento;
    private List<Movimentacao> compras = new ArrayList<>();
    private double total;

    public Fatura() {
        this.mesReferencia = LocalDate.now().getMonthValue() + "/" + LocalDate.now().getYear();
        this.dataFechamento = LocalDate.now().withDayOfMonth(25);
        this.dataVencimento = dataFechamento.plusDays(10);
        this.total = 0.0;
    }

    public void adicionarCompra(String descricao, double valor) {
        compras.add(Movimentacao.of("Compra Crédito: " + descricao, valor));
        total += valor;
    }

    public List<Movimentacao> getCompras() { return compras; }
    public double getTotal() { return total; }
    public String getMesReferencia() { return mesReferencia; }
    public LocalDate getDataFechamento() { return dataFechamento; }
    public LocalDate getDataVencimento() { return dataVencimento; }

    public void pagar() {
        compras.clear();
        total = 0.0;
    }

    @Override
    public String toString() {
        return String.format("Fatura %s - Total: R$ %.2f - Vencimento: %s",
                mesReferencia, total, dataVencimento);
    }
    public Fatura(double total, List<Movimentacao> compras) {
    this();
    this.total = total;
    if (compras != null) this.compras = compras;
}
}
