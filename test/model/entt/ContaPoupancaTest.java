package model.entt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import model.entt.ContaPoupanca;
import model.excp.DomainException;

public class ContaPoupancaTest {

    private ContaPoupanca conta;

    @BeforeEach
    void setup() {
        conta = new ContaPoupanca(2, "Paulo", "4321", 200.0);
    }

    @Test
    void deveInvestirComRendimento() {
        conta.investir(100.0);
        assertEquals(100.0, conta.getSaldo()); 
        assertTrue(conta.getInvestimento() > 100.0); 
    }

    @Test
    void deveResgatarInvestimento() {
        conta.investir(100.0);
        double antes = conta.getSaldo();
        conta.resgatar(50.0);
        assertTrue(conta.getInvestimento() < 150.0);
        assertTrue(conta.getSaldo() > antes);
    }

    @Test
    void deveLancarErroAoInvestirValorInvalido() {
        assertThrows(DomainException.class, () -> conta.investir(-10.0));
    }

    @Test
    void deveLancarErroAoResgatarMaisQueInvestido() {
        conta.investir(50.0);
        assertThrows(DomainException.class, () -> conta.resgatar(500.0));
    }

    @Test
    void deveRetornarTipoContaCorreto() {
        assertEquals("Conta Poupança", conta.getTipoConta());
    }
}
