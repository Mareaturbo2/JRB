package service;

import com.google.gson.*;
import model.entt.*;
import model.excp.DomainException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class BankService {
    private static final String FILE_PATH = "data/contas.json";
    private final Map<String, Account> contas = new HashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public BankService() {
        carregar();
    }

    
    // busca e validação
    public Account buscarConta(String cpf) {
        return contas.get(cpf);
    }

    public boolean validarSenha(Account conta, String senhaDigitada) {
        return conta != null && conta.validarSenha(senhaDigitada);
    }

    // deposito, saque e cancelamento
    public void depositar(String cpf, double valor) {
        Account c = contas.get(cpf);
        if (c == null) throw new DomainException("Conta não encontrada.");
        c.depositar(valor);
        salvar();
    }

    public void sacar(String cpf, double valor) {
        Account c = contas.get(cpf);
        if (c == null) throw new DomainException("Conta não encontrada.");
        c.sacar(valor);
        salvar();
    }

    public void encerrarConta(String cpf) {
    Account conta = contas.get(cpf);
    if (conta == null)
        throw new DomainException("Conta não encontrada.");

    //verifica se tem saldo positivo
    if (conta.getSaldo() > 0)
        throw new DomainException("Conta não pode ser encerrada com saldo disponível.");

    //verifica se está usando cheque especial (saldo negativo)
    if (conta.getSaldo() < 0)
        throw new DomainException("Conta não pode ser encerrada com cheque especial em uso.");

    //verifica se tem fatura de cartão de crédito em aberto
    if (conta.getCartaoCredito() != null
            && conta.getCartaoCredito().getFaturaAtual() != null
            && conta.getCartaoCredito().getFaturaAtual().getTotal() > 0)
        throw new DomainException("Conta possui fatura de cartão de crédito pendente.");

    //verifica se tem investimento na poupança (valor aplicado)
    if (conta instanceof ContaPoupanca cp && cp.getInvestimento() > 0)
        throw new DomainException("Conta não pode ser encerrada com dinheiro investido na poupança.");

    // tudo certo, cancela a conta
    conta.encerrar();
    salvar();
}


    // pagamento de boleto
    
    public void pagarBoleto(String cpf, String codigo, double valor, String dataVencimento) {
        Account conta = contas.get(cpf);
        if (conta == null) throw new DomainException("Conta não encontrada.");
        if (conta.isEncerrada()) throw new DomainException("Conta encerrada.");
        if (valor <= 0) throw new DomainException("Valor inválido para pagamento.");
        if (valor > conta.getSaldo()) throw new DomainException("Saldo insuficiente para pagamento.");

        conta.debitarInterno(valor, "Pagamento de boleto " + codigo +
                (dataVencimento != null && !dataVencimento.isBlank()
                        ? " (Venc.: " + dataVencimento + ")" : ""));

        salvar();
        gerarComprovantePagamento(conta, codigo, valor, dataVencimento);
    }

    private void gerarComprovantePagamento(Account conta, String codigo, double valor, String dataVencimento) {
        try {
            File pasta = new File("data");
            if (!pasta.exists()) pasta.mkdirs();

            String nomeArquivo = "data/comprovante_boleto_" + codigo + ".pdf";
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(nomeArquivo));
            doc.open();

            doc.add(new Paragraph("Comprovante de Pagamento de Boleto"));
            doc.add(new Paragraph("Titular: " + conta.getTitular()));
            doc.add(new Paragraph("Número da Conta: " + conta.getNumero()));
            doc.add(new Paragraph("Código do Boleto: " + codigo));
            if (dataVencimento != null && !dataVencimento.isBlank())
                doc.add(new Paragraph("Data de Vencimento: " + dataVencimento));
            doc.add(new Paragraph("Valor Pago: R$ " + String.format("%.2f", valor)));
            doc.add(new Paragraph("Data do Pagamento: " +
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            doc.add(new Paragraph("Status: Pagamento realizado com sucesso."));
            doc.close();
        } catch (Exception e) {
            System.out.println("Erro ao gerar comprovante: " + e.getMessage());
        }
    }

    // transferencia
    public void transferir(String cpfOrigem, String cpfDestino, double valor) {
    Account origem = contas.get(cpfOrigem);
    Account destino = contas.get(cpfDestino);

    if (origem == null)
        throw new DomainException("Conta de origem não encontrada.");
    if (destino == null)
        throw new DomainException("Conta de destino não encontrada.");
    if (origem.isEncerrada())
        throw new DomainException("Conta de origem encerrada.");
    if (destino.isEncerrada())
        throw new DomainException("Conta de destino encerrada.");
    if (valor <= 0)
        throw new DomainException("Valor inválido para transferência.");

    //verifica se é conta corrente (pode usar cheque especial)
    double limiteDisponivel = origem.getSaldo();
    if (origem instanceof ContaCorrente cc) {
        limiteDisponivel += cc.getLimiteChequeEspecial();
    }

    //verifica se tem limite suficiente
    if (valor > limiteDisponivel)
        throw new DomainException("Saldo insuficiente para transferência (limite de cheque especial excedido).");

    //faz a transferencia
    origem.setSaldo(origem.getSaldo() - valor);
    destino.setSaldo(destino.getSaldo() + valor);

    origem.getMovimentacoes().add(Movimentacao.of("Transferência enviada para CPF " + cpfDestino, -valor));
    destino.getMovimentacoes().add(Movimentacao.of("Transferência recebida de CPF " + cpfOrigem, valor));

    salvar();
    gerarComprovanteTransferencia(origem, destino, valor);
}

    private void gerarComprovanteTransferencia(Account origem, Account destino, double valor) {
        try {
            File pasta = new File("data");
            if (!pasta.exists()) pasta.mkdirs();

            String codigo = java.util.UUID.randomUUID().toString().substring(0, 8);
            String nomeArquivo = "data/comprovante_transferencia_" + codigo + ".pdf";
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(nomeArquivo));
            doc.open();

            doc.add(new Paragraph("Comprovante de Transferência Entre Contas"));
            doc.add(new Paragraph("Conta de Origem: " + origem.getTitular() + " (" + origem.getNumero() + ")"));
            doc.add(new Paragraph("Conta de Destino: " + destino.getTitular() + " (" + destino.getNumero() + ")"));
            doc.add(new Paragraph("Valor: R$ " + String.format("%.2f", valor)));
            doc.add(new Paragraph("Data: " +
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            doc.add(new Paragraph("Status: Transferência realizada com sucesso."));
            doc.close();
        } catch (Exception e) {
            System.out.println("Erro ao gerar comprovante: " + e.getMessage());
        }
    }

    // cria a conta
    
    public Account criarConta(String nome, String cpf, String senha, Double saldoInicial, String tipo) {
        if (nome == null || nome.isBlank()) throw new DomainException("Nome inválido");
        if (cpf == null || cpf.isBlank()) throw new DomainException("CPF inválido");
        if (senha == null || senha.isBlank()) throw new DomainException("Senha inválida");
        if (contas.containsKey(cpf)) throw new DomainException("CPF já vinculado a uma conta");
        if (saldoInicial == null) saldoInicial = 0.0;

        int numero = NumberGenerator.gerarNumeroConta();
        Account conta;
        switch (tipo.toLowerCase()) {
            case "corrente" -> conta = new ContaCorrente(numero, nome, senha, saldoInicial);
            case "poupanca" -> conta = new ContaPoupanca(numero, nome, senha, saldoInicial);
            default -> throw new DomainException("Tipo de conta inválido (use: corrente/poupanca)");
        }

        contas.put(cpf, conta);
        salvar();
        return conta;
    }

    //json
    public void salvar() {
        try {
            File pasta = new File("data");
            if (!pasta.exists()) pasta.mkdirs();

            JsonObject root = new JsonObject();
            for (Map.Entry<String, Account> e : contas.entrySet()) {
                String cpf = e.getKey();
                Account c = e.getValue();
                JsonObject obj = new JsonObject();
                obj.addProperty("cpf", cpf);
                obj.addProperty("tipo", c.getTipoConta().equals("Conta Corrente") ? "corrente" : "poupanca");
                obj.addProperty("conta", c.getNumero());
                obj.addProperty("titular", c.getTitular());
                obj.addProperty("senha", c.getSenhaMaskless());
                obj.addProperty("saldo", c.getSaldo());
                obj.addProperty("encerrada", c.isEncerrada());
                obj.addProperty("dataEncerramento", c.getDataEncerramento());

                //poupança
                if (c instanceof ContaPoupanca cp) {
                    JsonObject jp = new JsonObject();
                    jp.addProperty("investimento", cp.getInvestimento());
                    jp.addProperty("ultimaAplicacao", cp.getUltimaAplicacao().toString());
                    obj.add("poupanca", jp);
                }

                //cartao
                if (c.getCartaoCredito() != null) {
                    CartaoCredito cc = c.getCartaoCredito();
                    JsonObject jc = new JsonObject();
                    jc.addProperty("numero", cc.getNumero());
                    jc.addProperty("validade", cc.getValidade());
                    jc.addProperty("cvv", cc.getCvv());
                    jc.addProperty("limite", cc.getLimite());
                    JsonObject jf = new JsonObject();
                    jf.addProperty("total", cc.getFaturaAtual().getTotal());
                    JsonArray compras = new JsonArray();
                    for (Movimentacao m : cc.getFaturaAtual().getCompras()) {
                        JsonObject jm = new JsonObject();
                        jm.addProperty("tipo", m.getTipo());
                        jm.addProperty("valor", m.getValor());
                        jm.addProperty("dataHora", m.getDataHora());
                        compras.add(jm);
                    }
                    jf.add("compras", compras);
                    jc.add("fatura", jf);
                    obj.add("cartaoCredito", jc);
                }

                if (c.getCartaoDebito() != null) {
                    CartaoDebito cd = c.getCartaoDebito();
                    JsonObject jd = new JsonObject();
                    jd.addProperty("numero", cd.getNumero());
                    jd.addProperty("validade", cd.getValidade());
                    jd.addProperty("cvv", cd.getCvv());
                    obj.add("cartaoDebito", jd);
                }

                //movimentação
                JsonArray movs = new JsonArray();
                for (Movimentacao m : c.getMovimentacoes()) {
                    JsonObject jm = new JsonObject();
                    jm.addProperty("tipo", m.getTipo());
                    jm.addProperty("valor", m.getValor());
                    jm.addProperty("dataHora", m.getDataHora());
                    movs.add(jm);
                }
                obj.add("movimentacoes", movs);

                root.add(cpf, obj);
            }

            try (Writer w = new OutputStreamWriter(new FileOutputStream(FILE_PATH), StandardCharsets.UTF_8)) {
                gson.toJson(root, w);
            }
        } catch (IOException ex) {
            System.out.println("Erro ao salvar contas: " + ex.getMessage());
        }
    }

    private void carregar() {
        File f = new File(FILE_PATH);
        if (!f.exists()) return;
        try (Reader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                String cpf = entry.getKey();
                JsonObject obj = entry.getValue().getAsJsonObject();

                String tipo = obj.get("tipo").getAsString();
                int numero = obj.get("conta").getAsInt();
                String titular = obj.get("titular").getAsString();
                String senha = obj.get("senha").getAsString();
                double saldo = obj.get("saldo").getAsDouble();

                Account conta = switch (tipo) {
                    case "corrente" -> new ContaCorrente(numero, titular, senha, saldo);
                    case "poupanca" -> new ContaPoupanca(numero, titular, senha, saldo);
                    default -> null;
                };
                if (conta == null) continue;

                //carregar dados da poupança
                if ("poupanca".equals(tipo) && obj.has("poupanca")) {
                    JsonObject jp = obj.getAsJsonObject("poupanca");
                    double investimento = jp.has("investimento") ? jp.get("investimento").getAsDouble() : 0.0;
                    String data = jp.has("ultimaAplicacao") ? jp.get("ultimaAplicacao").getAsString() : null;

                    ContaPoupanca cp = (ContaPoupanca) conta;
                    cp.setInvestimento(investimento);
                    if (data != null && !data.isBlank()) {
                        cp.setUltimaAplicacao(java.time.LocalDate.parse(data));
                    }
                }

                //cancelamento
                if (obj.has("encerrada") && obj.get("encerrada").getAsBoolean()) {
                    conta.encerrar();
                    if (obj.has("dataEncerramento") && !obj.get("dataEncerramento").isJsonNull()) {
                        java.lang.reflect.Field fEnc = Account.class.getDeclaredField("dataEncerramento");
                        fEnc.setAccessible(true);
                        fEnc.set(conta, obj.get("dataEncerramento").getAsString());
                    }
                }

                //movimentação
                List<Movimentacao> movsList = new ArrayList<>();
                JsonArray movs = obj.has("movimentacoes") ? obj.get("movimentacoes").getAsJsonArray() : new JsonArray();
                for (JsonElement el : movs) {
                    JsonObject jm = el.getAsJsonObject();
                    movsList.add(new Movimentacao(
                            jm.get("tipo").getAsString(),
                            jm.get("valor").getAsDouble(),
                            jm.get("dataHora").getAsString()));
                }
                conta.setMovimentacoes(movsList);

                //cartão
                if (obj.has("cartaoCredito")) {
                    JsonObject jc = obj.getAsJsonObject("cartaoCredito");
                    double limite = jc.has("limite") ? jc.get("limite").getAsDouble() : 0.0;
                    Fatura fatura = carregarFatura(jc.getAsJsonObject("fatura"));
                    CartaoCredito cc = new CartaoCredito(
                            jc.get("numero").getAsString(),
                            jc.get("validade").getAsString(),
                            jc.get("cvv").getAsString(),
                            limite,
                            fatura
                    );
                    conta.setCartaoCredito(cc);
                }

                if (obj.has("cartaoDebito")) {
                    JsonObject jd = obj.getAsJsonObject("cartaoDebito");
                    CartaoDebito cd = new CartaoDebito(
                            jd.get("numero").getAsString(),
                            jd.get("validade").getAsString(),
                            jd.get("cvv").getAsString()
                    );
                    conta.setCartaoDebito(cd);
                }

                contas.put(cpf, conta);
            }
        } catch (Exception ex) {
            System.out.println("Erro ao carregar contas: " + ex.getMessage());
        }
    }

    private Fatura carregarFatura(JsonObject jf) {
        if (jf == null) return new Fatura();
        double total = jf.has("total") ? jf.get("total").getAsDouble() : 0.0;
        List<Movimentacao> compras = new ArrayList<>();
        if (jf.has("compras")) {
            JsonArray comprasArr = jf.getAsJsonArray("compras");
            for (JsonElement je : comprasArr) {
                JsonObject jm = je.getAsJsonObject();
                compras.add(new Movimentacao(
                        jm.get("tipo").getAsString(),
                        jm.get("valor").getAsDouble(),
                        jm.get("dataHora").getAsString()));
            }
        }
        return new Fatura(total, compras);
    }
}
