package service;

import java.util.Random;

public class NumberGenerator {

    private static final Random random = new Random();

    public static int gerarNumeroConta() {
        return 10000 + random.nextInt(90000);
    }

    public static String gerarNumeroCartao() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
            if ((i + 1) % 4 == 0 && i < 15) sb.append(" ");
        }
        return sb.toString();
    }

    public static String gerarCVV() {
        return String.format("%03d", random.nextInt(1000));
    }
}
