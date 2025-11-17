package service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

public class BankPersistenceTest {

    @Test
    void deveSalvarECarregarContas() {

        BankService bankOriginal = new BankService();
        bankOriginal.criarConta("Paulo", "123", "9999", 500.0, "corrente");
        bankOriginal.salvar();

        File file = new File("data/contas.json");
        assertTrue(file.exists(), "Arquivo JSON não foi gerado!");

        // Carrega de novo para validar persistência
        BankService bankNovo = new BankService();
        assertNotNull(bankNovo.buscarConta("123"), "Conta não carregada do JSON!");
        assertEquals(500.0, bankNovo.buscarConta("123").getSaldo());
    }
}
