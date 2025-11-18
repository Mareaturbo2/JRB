package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import model.entt.Account;
import model.entt.ContaCorrente;

import java.io.File;
import java.time.LocalDate;

class ExtratoServiceTest {

    private Account conta;
    private ExtratoService extratoService;

    @BeforeEach
    void setup() {
        conta = new ContaCorrente(1, "Paulo", "1234", 1000.0);
        extratoService = new ExtratoService();

        // simula transações reais do sistema
        conta.depositar(300.0);
        conta.sacar(100.0);
    }

    @Test
    void deveMostrarExtratoCompletoSemErro() {
        assertDoesNotThrow(() -> extratoService.mostrarCompleto(conta));
        assertTrue(conta.getMovimentacoes().size() > 0);
    }

    @Test
    void deveMostrarExtratoFiltradoSemErro() {
        LocalDate hoje = LocalDate.now();
        assertDoesNotThrow(() ->
                extratoService.mostrarFiltrado(conta, hoje.minusDays(1), hoje.plusDays(1))
        );
    }

    @Test
    void deveGerarPDFComSucesso() {
        extratoService.exportarExtratoPDF(conta);

        File pdf = new File("data/extrato_" + conta.getNumero() + ".pdf");
        assertTrue(pdf.exists());
        assertTrue(pdf.length() > 0);
    }
}
