package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import model.entt.Account;
import model.excp.DomainException;

public class EncerramentoErrosTest {

    private BankService bank;

    @BeforeEach
    void setup() throws Exception {
        bank = new BankService();

        // Limpando JSON carregado
        java.lang.reflect.Field field = BankService.class.getDeclaredField("contas");
        field.setAccessible(true);
        ((java.util.Map<?, ?>) field.get(bank)).clear();

        bank.criarConta("Paulo", "111", "1234", 100.0, "corrente");
    }

    @Test
    void naoDeveEncerrarContaComSaldoPositivo() {
        assertThrows(DomainException.class,
                () -> bank.encerrarConta("111"));
    }

    @Test
    void naoDeveEncerrarContaComSaldoNegativo() {
        Account conta = bank.buscarConta("111");
        conta.sacar(150.0); // entra no cheque especial (-50)
        assertThrows(DomainException.class,
                () -> bank.encerrarConta("111"));
    }

    @Test
    void naoDeveEncerrarContaInexistente() {
        assertThrows(DomainException.class,
                () -> bank.encerrarConta("999"));
    }
}
