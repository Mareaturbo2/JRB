package model.entt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import model.entt.ContaCorrente;
import model.excp.DomainException;


public class ContaCorrenteTest {

    private ContaCorrente conta;

    @BeforeEach
    void setup() {
        conta = new ContaCorrente(1, "Paulo", "1234", 100.0);
    }

    @Test
    void devePermitirUsoDoChequeEspecial() {
        conta.sacar(550.0); // 100 saldo + 500 limite
        assertEquals(-450.0, conta.getSaldo());
        assertEquals(450.0, conta.getValorUsadoChequeEspecial());
    }

    @Test
    void deveLancarErroAoExcederLimiteChequeEspecial() {
        assertThrows(DomainException.class, () -> conta.sacar(700.0));
    }

    @Test
    void deveAtualizarLimiteChequeEspecial() {
        conta.setLimiteChequeEspecial(1000.0);
        assertEquals(1000.0, conta.getLimiteChequeEspecial());
    }

    @Test
    void deveRetornarTipoContaCorreto() {
        assertEquals("Conta Corrente", conta.getTipoConta());
    }
}
