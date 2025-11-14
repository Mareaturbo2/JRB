package service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class NumberGeneratorTest {

  @Test
    void deveGerarNumeroCartaoValido() {
    String numero = NumberGenerator.gerarNumeroCartao();

    
    String digitsOnly = numero.replaceAll("\\D", "");

    assertEquals(16, digitsOnly.length());
    assertTrue(digitsOnly.matches("\\d+"));
}

    @Test
    void deveGerarCVVValido() {
        String cvv = NumberGenerator.gerarCVV();
        assertNotNull(cvv);
        assertEquals(3, cvv.length());
    }
}
