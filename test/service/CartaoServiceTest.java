package service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import model.entt.Account;
import model.entt.ContaCorrente;
import model.entt.ContaPoupanca;
import model.excp.DomainException;

public class CartaoServiceTest {

    private BankService bank;
    private CartaoService cartaoService;

@BeforeEach
void setup() throws Exception {
    bank = new BankService();
    cartaoService = new CartaoService(bank);

    // limpa as contas antes de recriar
    java.lang.reflect.Field field = BankService.class.getDeclaredField("contas");
    field.setAccessible(true);
    ((java.util.Map<?, ?>) field.get(bank)).clear();

    bank.criarConta("Paulo", "111", "1234", 1000.0, "corrente");
    bank.criarConta("Maria", "222", "5678", 2000.0, "poupanca");
}

    @Test
    void deveSolicitarCartaoCreditoComSucesso() {
        cartaoService.solicitarCartaoCredito("111", 500.0);
        Account conta = bank.buscarConta("111");
        assertNotNull(conta.getCartaoCredito());
        assertEquals(500.0, conta.getCartaoCredito().getLimite());
    }

    @Test
    void deveLancarErroAoSolicitarCartaoCreditoContaPoupanca() {
        assertThrows(RuntimeException.class,
            () -> cartaoService.solicitarCartaoCredito("222", 500.0));
    }

    @Test
    void deveLancarErroAoSolicitarCartaoCreditoDuasVezes() {
        cartaoService.solicitarCartaoCredito("111", 500.0);
        assertThrows(DomainException.class,
            () -> cartaoService.solicitarCartaoCredito("111", 500.0));
    }

    @Test
    void deveSolicitarCartaoDebitoComSucesso() {
        cartaoService.solicitarCartaoDebito("111");
        Account conta = bank.buscarConta("111");
        assertNotNull(conta.getCartaoDebito());
    }

    @Test
    void deveComprarNoDebitoComSucesso() {
        cartaoService.solicitarCartaoDebito("111");
        cartaoService.comprarDebito("111", 100.0, "Mercado");

        Account conta = bank.buscarConta("111");
        assertEquals(900.0, conta.getSaldo());
    }

    @Test
    void deveComprarNoCreditoComSucesso() {
        cartaoService.solicitarCartaoCredito("111", 1000.0);
        cartaoService.comprarCredito("111", 300.0, "Padaria");

        Account conta = bank.buscarConta("111");
        assertEquals(700.0, conta.getCartaoCredito().getLimiteDisponivel());
    }

    @Test
    void deveLancarErroComprarDebitoSemCartao() {
        assertThrows(DomainException.class,
            () -> cartaoService.comprarDebito("111", 100.0, "Compra"));
    }

    @Test
    void deveLancarErroComprarCreditoSemCartao() {
        assertThrows(DomainException.class,
            () -> cartaoService.comprarCredito("111", 100.0, "Compra"));
    }

    @Test
    void deveLancarErroAoPagarFaturaSemCartao() {
        assertThrows(DomainException.class,
            () -> cartaoService.pagarFatura("111"));
    }

    @Test
    void devePagarFaturaComSucesso() {
        cartaoService.solicitarCartaoCredito("111", 1000.0);
        cartaoService.comprarCredito("111", 300.0, "Loja");

        cartaoService.pagarFatura("111");
        Account conta = bank.buscarConta("111");

        assertEquals(700.0, conta.getSaldo());
        assertEquals(0.0, conta.getCartaoCredito().getFaturaTotal());
    }
}
