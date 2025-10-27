package model.entt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import model.entt.Account;
import model.entt.ContaCorrente;
import model.excp.DomainException;
public class AccountTest {

    private Account conta;

    @BeforeEach
    void setup() {
        conta = new ContaCorrente(1, "Paulo", "1234", 100.0);
        
    }

    @Test
    void deveDepositarComSucesso() {
        conta.depositar(50.0);
        assertEquals(150.0, conta.getSaldo());
    }

    @Test
    void deveLancarErroAoDepositarValorInvalido() {
        assertThrows(DomainException.class, () -> conta.depositar(-10.0));
    }

    @Test
    void deveSacarComSucesso() {
        conta.sacar(30.0);
        assertEquals(70.0, conta.getSaldo());
    }

    @Test
    void deveLancarErroAoSacarMaisQueSaldo() {
        assertThrows(DomainException.class, () -> conta.sacar(700.0));
    }

    @Test
    void deveValidarSenhaCorreta() {
        assertTrue(conta.validarSenha("1234"));
    }

    @Test
    void deveBloquearDepoisDe3TentativasErradas() {
        conta.registrarTentativaSenha(false);
        conta.registrarTentativaSenha(false);
        conta.registrarTentativaSenha(false);
        assertTrue(conta.estaBloqueada());
    }
}
