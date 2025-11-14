package model.entt;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FaturaTest {

    private Fatura fatura;

    @BeforeEach
    void setup() {
        fatura = new Fatura();
    }

    @Test
    void deveAdicionarCompraNaFatura() {
        fatura.adicionarCompra("Mercado", 100.0);
        assertEquals(100.0, fatura.getTotal());
    }

    @Test
    void devePagarFatura() {
        fatura.adicionarCompra("Loja", 200.0);
        fatura.pagar();
        assertEquals(0.0, fatura.getTotal());
    }

    @Test
    void deveListarMovimentacoes() {
        fatura.adicionarCompra("Teste1", 10.0);
        assertEquals(1, fatura.getCompras().size());
    }
}
