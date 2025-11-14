package model.entt;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class MovimentacaoTest {

    @Test
    void deveCriarMovimentacaoComDadosInformados() {
        Movimentacao mov = new Movimentacao("Depósito", 100.0, "10/10/2025 12:00");

        assertEquals("Depósito", mov.getTipo());
        assertEquals(100.0, mov.getValor());
        assertEquals("10/10/2025 12:00", mov.getDataHora());
        assertTrue(mov.toString().contains("Depósito"));
    }

    @Test
    void deveCriarMovimentacaoUsandoFactoryOf() {
        Movimentacao mov = Movimentacao.of("Saque", 50.0);

        assertEquals("Saque", mov.getTipo());
        assertEquals(50.0, mov.getValor());
        assertNotNull(mov.getDataHora());
        assertTrue(mov.getDataHora().contains("/")); 
    }

    @Test
    void deveFormatarToStringCorretamente() {
    Movimentacao m = Movimentacao.of("Saque", -50.0);
    String out = m.toString();

    assertTrue(out.contains("Saque"));
    assertTrue(out.contains("R$"));
    assertTrue(out.contains("-50"));
    assertTrue(out.matches(".*\\d{2}/\\d{2}/\\d{4}.*")); 
}

}
