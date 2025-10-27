package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import model.entt.ContaCorrente;
import model.entt.ContaPoupanca;
import model.entt.Account;
import model.excp.DomainException;
import service.BankService;


public class BankServiceTest {

    private BankService service;

  @BeforeEach
        void setup() throws Exception {
    service = new BankService();

    
    java.lang.reflect.Field field = BankService.class.getDeclaredField("contas");
    field.setAccessible(true);
    ((java.util.Map<?, ?>) field.get(service)).clear();

    service.criarConta("Paulo", "0101012", "1234", 100.0, "corrente");
    service.criarConta("Maria", "0202022", "5678", 200.0, "poupanca");
}

    @Test
    void deveDepositarEmConta() {
        service.depositar("0101012", 50.0);
        assertEquals(150.0, service.buscarConta("0101012").getSaldo());
    }

    @Test
    void deveTransferirEntreContas() {
        service.transferir("0101012", "0202022", 50.0);
        assertEquals(50.0, service.buscarConta("0101012").getSaldo());
        assertEquals(250.0, service.buscarConta("0202022").getSaldo());
    }

    @Test
    void deveLancarErroAoTransferirSaldoInsuficiente() {
        assertThrows(DomainException.class, () -> service.transferir("0101012", "0202022", 1000.0));
    }

    @Test
    void deveEncerrarContaComSaldoZero() {
        Account c = service.buscarConta("0101012");
        c.setSaldo(0.0);
        service.encerrarConta("0101012");
        assertTrue(c.isEncerrada());
    }

    @Test
    void deveLancarErroAoEncerrarComSaldoPositivo() {
        assertThrows(DomainException.class, () -> service.encerrarConta("0202022"));
    }
}
