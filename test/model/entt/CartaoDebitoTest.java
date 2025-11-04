package model.entt;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import model.excp.DomainException;

public class CartaoDebitoTest {

    private ContaCorrente conta;
    private CartaoDebito cartao;

    @BeforeEach
    void setup() {
        conta = new ContaCorrente(1, "Paulo", "1234", 100.0);
        cartao = new CartaoDebito();
        conta.setCartaoDebito(cartao);
    }

    @Test
    void devePermitirCompraSeHouverSaldo() {
        cartao.comprar(conta, 50.0, "Mercado");
        assertEquals(50.0, conta.getSaldo());
    }

    @Test
    void deveLancarErroSeSaldoInsuficiente() {
        assertThrows(DomainException.class, () -> {
            cartao.comprar(conta, 200.0, "Loja");
        });
    }

    @Test
    void deveLancarErroParaValorInvalido() {
        assertThrows(DomainException.class, () -> {
            cartao.comprar(conta, -10.0, "Teste");
        });
    }
}
