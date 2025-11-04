package model.entt;

import model.excp.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class CartaoCreditoTest {

    private ContaCorrente conta;
    private CartaoCredito cartao;

   @BeforeEach
        void setup() {
            conta = new ContaCorrente(1, "Paulo", "1234", 1000.0);
            cartao = new CartaoCredito(500.0);
            conta.setCartaoCredito(cartao);
        }

   @Test
    void devePermitirCompraDentroDoLimite() {
    cartao.comprar("Mercado", 100.0);
     assertEquals(1000.0, conta.getSaldo(), 0.001);
    assertEquals(100.0, cartao.getFaturaTotal(), 0.001);
    }


    @Test
    void deveRecusarCompraAcimaDoLimite() {
        assertThrows(DomainException.class, () -> cartao.comprar("TV", 600.0));
    }

    @Test
    void devePagarFaturaComSaldoSuficiente() {
        cartao.comprar("Roupas", 200.0);
        cartao.pagarFatura(conta);
        assertEquals(800.0, conta.getSaldo(), 0.001);
    }

    @Test
    void deveRecusarPagamentoSemSaldo() {
        cartao.comprar("Celular", 400.0);
        conta.sacar(900.0); // deixa sem saldo

        assertThrows(DomainException.class, () -> cartao.pagarFatura(conta));
    }
}
