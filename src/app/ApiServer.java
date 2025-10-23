package app;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.File;
import java.lang.reflect.Type;
import model.entt.Account;
import model.entt.CartaoCredito;
import model.entt.CartaoDebito;
import model.entt.ContaCorrente;
import model.entt.ContaPoupanca;
import model.entt.Fatura;
import model.entt.Movimentacao;
import service.BankService;
import service.CartaoService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

// iText (para gerar PDFs)
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Element;


public class ApiServer {

    public static void main(String[] args) {
        port(8080);
        staticFiles.externalLocation(new File("data").getAbsolutePath());

        final BankService bankService = new BankService();
        final CartaoService cartaoService = new CartaoService(bankService);
            final Gson gson = new GsonBuilder()
                //suporte para local date
                .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
                    @Override
                    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(src.toString());
                    }
                })
                .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
                    @Override
                    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return LocalDate.parse(json.getAsString());
                    }
                })
                //suporte para local data time
                .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                    @Override
                    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(src.toString());
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                    @Override
                    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return LocalDateTime.parse(json.getAsString());
                    }
                })
                .setPrettyPrinting()
                .create();

        
        // CORS básico para o front
        
        options("/*", (req, res) -> {
            String reqHeaders = req.headers("Access-Control-Request-Headers");
            if (reqHeaders != null) res.header("Access-Control-Allow-Headers", reqHeaders);
            String reqMethod = req.headers("Access-Control-Request-Method");
            if (reqMethod != null) res.header("Access-Control-Allow-Methods", reqMethod);
            return "OK";
        });

        before((req, res) -> {
                res.header("Access-Control-Allow-Origin", "*");
                res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                res.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
            });

        
        // Status
        
        get("/api/status", (req, res) -> "Servidor ativo!");


          post("/api/contas", (req, res) -> {
                res.type("application/json");
                Map<String, Object> data = gson.fromJson(req.body(), Map.class);

                String cpf = (String) data.get("cpf");
                String nome = (String) data.get("titular");
                String tipo = (String) data.get("tipo");
                Double saldoInicial = data.get("saldoInicial") == null ? 0.0 : ((Number) data.get("saldoInicial")).doubleValue();
                String senha = String.valueOf(data.get("senha"));

                try {
                    
                    bankService.criarConta(nome, cpf, senha, saldoInicial, tipo);

                    return gson.toJson("Conta criada com sucesso!");
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson("Erro ao criar conta: " + e.getMessage());
                }
            });

            // login

        
        // Login 
        
        post("/api/login", (req, res) -> {
            res.type("application/json");

            Map<String, Object> data = gson.fromJson(req.body(), Map.class);
            String cpf = (String) data.get("cpf");
            String senha = String.valueOf(data.get("senha"));

            Account conta = bankService.buscarConta(cpf);

            if (conta == null) {
                res.status(404);
                return gson.toJson(Map.of("erro", "Conta não encontrada."));
            }

            if (conta.isEncerrada()) {
                res.status(403);
                return gson.toJson(Map.of("erro", "Conta encerrada."));
            }

            //verifica senha e retorna json completo
            if (conta.validarSenha(senha)) {
                JsonObject contaJson = gson.toJsonTree(conta).getAsJsonObject();

                // Adiciona tipo dinamicamente
                if (conta instanceof model.entt.ContaPoupanca) {
                    contaJson.addProperty("tipo", "poupanca");
                } else {
                    contaJson.addProperty("tipo", "corrente");
                }

                //retorna todos os dados da conta no login
                return gson.toJson(contaJson);
            } else {
                res.status(401);
                return gson.toJson(Map.of("erro", "CPF ou senha incorretos."));
            }
        });





        
        //buscar a conta pelo cpf
        
       get("/api/contas/:cpf", (req, res) -> {
            res.type("application/json");
            String cpf = req.params("cpf");
            Account conta = bankService.buscarConta(cpf);

            if (conta == null) {
                res.status(404);
                return gson.toJson(Map.of("erro", "Conta não encontrada."));
            }

            //cria o json da conta
            JsonObject contaJson = gson.toJsonTree(conta).getAsJsonObject();

            //adiciona o campo "tipo" manualmente com base na classe
            if (conta instanceof model.entt.ContaPoupanca) {
                contaJson.addProperty("tipo", "poupanca");
            } else {
                contaJson.addProperty("tipo", "corrente");
            }

            //retorna o json completo
            return gson.toJson(contaJson);
        });


        
        //saldo com cheque especial
        
        get("/api/contas/:cpf/saldo", (req, res) -> {
            res.type("application/json");
            String cpf = req.params("cpf");
            Account conta = bankService.buscarConta(cpf);

            if (conta == null) {
                res.status(404);
                return gson.toJson(Map.of("erro", "Conta não encontrada."));
            }

            Map<String, Object> payload = new HashMap<>();
            double saldo = conta.getSaldo();
            double saldoDisponivel = saldo;

            if (conta instanceof ContaCorrente cc) {
                double limite = cc.getLimiteChequeEspecial();
                saldoDisponivel += limite;
                payload.put("limiteChequeEspecial", limite);
            } 
            else if (conta instanceof ContaPoupanca cp) {
                // Pega os dados diretamente da conta poupança
                double investimento = cp.getInvestimento();
                saldoDisponivel += investimento;
                payload.put("investimento", investimento);
            }

            payload.put("saldo", saldo);
            payload.put("saldoDisponivel", saldoDisponivel);

            return gson.toJson(payload);
        });


        
        //deposito
        
            post("/api/contas/:cpf/deposito", (req, res) -> {
                res.type("application/json");
                String cpf = req.params("cpf");

                try {
                    JsonObject body = gson.fromJson(req.body(), JsonObject.class);
                    double valor = body.get("valor").getAsDouble();

                    bankService.depositar(cpf, valor);
                    res.status(200);
                    return gson.toJson("Depósito de R$ " + valor + " realizado com sucesso!");
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson("Erro no depósito: " + e.getMessage());
                }
            });


        
        //saque
        
        post("/api/contas/:cpf/saque", (req, res) -> {
                res.type("application/json");
                String cpf = req.params("cpf");

                try {
                    
                    JsonObject body = gson.fromJson(req.body(), JsonObject.class);
                    double valor = body.get("valor").getAsDouble();

                    bankService.sacar(cpf, valor);
                    res.status(200);
                    return gson.toJson("Saque de R$ " + valor + " realizado com sucesso!");
                } catch (Exception e) {
                    res.status(400);
                    return gson.toJson("Erro no saque: " + e.getMessage());
                }
            });



        
        //pagamento boleto
        
        post("/api/contas/:cpf/pagamento", (req, res) -> {
            res.type("application/json");
            String cpf = req.params("cpf");

            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            if (body == null || !body.containsKey("codigo") || !body.containsKey("valor")) {
                res.status(400);
                return gson.toJson(Map.of("erro", "Campos obrigatórios: codigo e valor"));
            }

            String codigo = String.valueOf(body.get("codigo"));
            double valor = ((Number) body.get("valor")).doubleValue();
            String dataVencimento = body.containsKey("dataVencimento") ? (String) body.get("dataVencimento") : null;

            try {
                
                bankService.pagarBoleto(cpf, codigo, valor, dataVencimento);

                return gson.toJson(Map.of(
                    "mensagem",
                    String.format(Locale.US, "Pagamento de R$ %.2f realizado com sucesso (%s)", valor, codigo)
                ));

            } catch (Exception e) {
                res.status(400);
                return gson.toJson(Map.of("erro", e.getMessage()));
            }
        });




        
        //transferência entre contas
        
        post("/api/contas/transferir", (req, res) -> {
            res.type("application/json");
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            if (body == null || !body.containsKey("cpfOrigem") || !body.containsKey("cpfDestino") || !body.containsKey("valor")) {
                res.status(400);
                return gson.toJson(Map.of("erro", "Campos obrigatórios: cpfOrigem, cpfDestino e valor"));
            }
            String cpfOrigem = (String) body.get("cpfOrigem");
            String cpfDestino = (String) body.get("cpfDestino");
            double valor = ((Number) body.get("valor")).doubleValue();
            try {
                bankService.transferir(cpfOrigem, cpfDestino, valor);
                return gson.toJson(Map.of("mensagem", String.format(Locale.US, "Transferência de R$ %.2f realizada com sucesso de %s para %s", valor, cpfOrigem, cpfDestino)));
            } catch (Exception e) {
                res.status(400);
                return gson.toJson(Map.of("erro", e.getMessage()));
            }
        });

        
        // extrato json
        
        get("/api/contas/:cpf/extrato", (req, res) -> {
            res.type("application/json");
            String cpf = req.params("cpf");
            Account conta = bankService.buscarConta(cpf);

            if (conta == null) {
                res.status(404);
                return gson.toJson(Map.of("erro", "Conta não encontrada."));
            }

            // 🔹 Filtro de período opcional (inicio e fim)
            String inicioStr = req.queryParams("inicio"); // yyyy-MM-dd
            String fimStr = req.queryParams("fim");       // yyyy-MM-dd

            List<Movimentacao> movs = conta.getMovimentacoes();

            // 🔹 Evita NullPointer e filtra apenas movimentações válidas
            movs = movs == null ? new ArrayList<>() :
                movs.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 🔹 Filtro de data, se informado
            if (inicioStr != null && fimStr != null) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                LocalDate inicio = LocalDate.parse(inicioStr);
                LocalDate fim = LocalDate.parse(fimStr);

                movs = movs.stream().filter(m -> {
                    try {
                        LocalDate dataMov = LocalDate.parse(m.getDataHora(), fmt);
                        return (dataMov.isEqual(inicio) || dataMov.isAfter(inicio)) &&
                            (dataMov.isEqual(fim) || dataMov.isBefore(fim));
                    } catch (Exception e) {
                        return false; // ignora datas mal formatadas
                    }
                }).collect(Collectors.toList());
            }

            // 🔹 Converte para JSON simples e serializável
                        List<Map<String, Object>> payload = movs.stream()
                            .map(m -> {
                                Map<String, Object> map = new HashMap<>();
                                map.put("dataHora", m.getDataHora());
                                map.put("tipo", m.getTipo());
                                map.put("valor", m.getValor());
                                return map;
                            })
                            .collect(Collectors.toList());

                        return gson.toJson(Map.of(
                            "cpf", cpf,
                            "saldoAtual", conta.getSaldo(),
                            "movimentacoes", payload
                        ));
        });


        
        // extrato em pdf
        
        get("/api/contas/:cpf/extrato/pdf", (req, res) -> {
            String cpf = req.params("cpf");
            Account conta = bankService.buscarConta(cpf);
            if (conta == null) {
                res.status(404);
                return "Conta não encontrada";
            }

            res.raw().setContentType("application/pdf");
            res.raw().setHeader("Content-Disposition",
                    "attachment; filename=extrato_" + cpf + ".pdf");

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, res.raw().getOutputStream());
            document.open();

            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font infoFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

            document.add(new Paragraph("Extrato da conta\n\n", tituloFont));
            document.add(new Paragraph("CPF: " + cpf, infoFont));
            document.add(new Paragraph("Titular: " + conta.getTitular(), infoFont));
            document.add(new Paragraph("Emitido em: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), infoFont));
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.addCell("Data/Hora");
            table.addCell("Tipo");
            table.addCell("Valor (R$)");

            for (Movimentacao m : conta.getMovimentacoes()) {
                table.addCell(m.getDataHora());
                table.addCell(m.getTipo());
                table.addCell(String.format(Locale.US, "%.2f", m.getValor()));
            }

            document.add(table);
            document.add(new Paragraph("\nSaldo atual: R$ " + String.format(Locale.US, "%.2f", conta.getSaldo()), infoFont));

            // Se for ContaCorrente, mostra também cheque especial disponível
            if (conta instanceof ContaCorrente cc) {
                double limite = cc.getLimiteChequeEspecial();
                document.add(new Paragraph("Cheque especial: R$ " + String.format(Locale.US, "%.2f", limite), infoFont));
                document.add(new Paragraph("Saldo disponível: R$ " + String.format(Locale.US, "%.2f", conta.getSaldo() + limite), infoFont));
            }

            document.close();
            return res.raw();
        });

        
        //listar os cartões
        
        get("/api/contas/:cpf/cartoes", (req, res) -> {
            res.type("application/json");
            String cpf = req.params("cpf");
            Account conta = bankService.buscarConta(cpf);
            if (conta == null) {
                res.status(404);
                return gson.toJson(Map.of("erro", "Conta não encontrada."));
            }

            // Se o seu modelo possuir getters diretos de cartões, adapte aqui.
            Map<String, Object> out = new HashMap<>();
            try {
                Object cc = conta.getCartaoCredito();
                Object cd = conta.getCartaoDebito();
                out.put("possuiCartaoCredito", cc != null);
                out.put("possuiCartaoDebito", cd != null);
            } catch (Exception e) {
                out.put("mensagem", "Modelo de cartões não disponível nesta conta.");
            }
            return gson.toJson(out);
        });

        
        //solicitar o cartão de credito
        
        post("/api/contas/:cpf/cartoes/credito", (req, res) -> {
            res.type("application/json");
            String cpf = req.params("cpf");
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            if (body == null || !body.containsKey("limite")) {
                res.status(400);
                return gson.toJson(Map.of("erro", "Campo 'limite' é obrigatório."));
            }
            double limite = ((Number) body.get("limite")).doubleValue();
            try {
                cartaoService.solicitarCartaoCredito(cpf, limite);
                return gson.toJson(Map.of("mensagem", "Cartão de crédito solicitado com sucesso."));
            } catch (Exception e) {
                res.status(400);
                return gson.toJson(Map.of("erro", e.getMessage()));
            }
        });

        
        //solicita o cartao de debito
        
        post("/api/contas/:cpf/cartoes/debito", (req, res) -> {
            res.type("application/json");
            String cpf = req.params("cpf");
            try {
                cartaoService.solicitarCartaoDebito(cpf);
                return gson.toJson(Map.of("mensagem", "Cartão de débito solicitado com sucesso."));
            } catch (Exception e) {
                res.status(400);
                return gson.toJson(Map.of("erro", e.getMessage()));
            }
        });

        
        //compra no CC
        
        post("/api/contas/:cpf/cartoes/credito/compra", (req, res) -> {
            res.type("application/json");
            String cpf = req.params("cpf");
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            if (body == null || !body.containsKey("valor") || !body.containsKey("descricao")) {
                res.status(400);
                return gson.toJson(Map.of("erro", "Campos obrigatórios: valor e descricao"));
            }
            double valor = ((Number) body.get("valor")).doubleValue();
            String descricao = (String) body.get("descricao");
            try {
                cartaoService.comprarCredito(cpf, valor, descricao);
                return gson.toJson(Map.of("mensagem", "Compra no crédito registrada com sucesso."));
            } catch (Exception e) {
                res.status(400);
                return gson.toJson(Map.of("erro", e.getMessage()));
            }
        });

        
        //compra no debito
        
        post("/api/contas/:cpf/cartoes/debito/compra", (req, res) -> {
            res.type("application/json");
            String cpf = req.params("cpf");
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            if (body == null || !body.containsKey("valor") || !body.containsKey("descricao")) {
                res.status(400);
                return gson.toJson(Map.of("erro", "Campos obrigatórios: valor e descricao"));
            }
            double valor = ((Number) body.get("valor")).doubleValue();
            String descricao = (String) body.get("descricao");
            try {
                cartaoService.comprarDebito(cpf, valor, descricao);
                return gson.toJson(Map.of("mensagem", "Compra no débito registrada com sucesso."));
            } catch (Exception e) {
                res.status(400);
                return gson.toJson(Map.of("erro", e.getMessage()));
            }
        });

        
        //paga a fatura do CC
        
        post("/api/contas/:cpf/cartoes/pagar-fatura", (req, res) -> {
            res.type("application/json");
            String cpf = req.params("cpf");
            try {
                cartaoService.pagarFatura(cpf);
                return gson.toJson(Map.of("mensagem", "Fatura paga com sucesso."));
            } catch (Exception e) {
                res.status(400);
                return gson.toJson(Map.of("erro", e.getMessage()));
            }
        });

        
        //fatura pdf
        
        get("/api/contas/:cpf/cartoes/:numero/fatura/pdf", (req, res) -> {
            String cpf = req.params("cpf");
            String numeroCartao = req.params("numero");

            Account conta = bankService.buscarConta(cpf);
            if (conta == null) {
                res.status(404);
                return "Conta não encontrada";
            }

            CartaoCredito cartao = conta.getCartaoCredito();
            if (cartao == null) {
                res.status(404);
                return "Cartão de crédito não encontrado";
            }

            Fatura fatura = cartao.getFaturaAtual();
            if (fatura == null) {
                res.status(404);
                return "Nenhuma fatura encontrada.";
            }

            res.raw().setContentType("application/pdf");
            res.raw().setHeader("Content-Disposition",
                    "attachment; filename=fatura_" + numeroCartao + "_" + cpf + ".pdf");

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, res.raw().getOutputStream());
            document.open();

            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font infoFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

            document.add(new Paragraph("Fatura do Cartão " + numeroCartao + "\n\n", tituloFont));
            document.add(new Paragraph("CPF: " + cpf, infoFont));
            document.add(new Paragraph("Titular: " + conta.getTitular(), infoFont));
            document.add(new Paragraph("Emitido em: " +
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), infoFont));
            document.add(new Paragraph("Vencimento: " + fatura.getDataVencimento(), infoFont));
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.addCell("Data");
            table.addCell("Descrição");
            table.addCell("Valor (R$)");

            if (!fatura.getCompras().isEmpty()) {
                for (Movimentacao compra : fatura.getCompras()) {
                    table.addCell(compra.getDataHora());
                    table.addCell(compra.getTipo());
                    table.addCell(String.format("%.2f", compra.getValor()));
                }
            } else {
                PdfPCell cell = new PdfPCell(new Phrase("Nenhuma compra registrada."));
                cell.setColspan(3);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            document.add(table);
            document.add(new Paragraph("\nTotal da Fatura: R$ " +
                    String.format("%.2f", fatura.getTotal()), infoFont));

            document.close();
            return res.raw();
        });


        
        //consulta informações do cartão de crédito
        
        get("/api/contas/:cpf/cartoes/credito/info", (req, res) -> {
            res.type("application/json");
            String cpf = req.params("cpf");

            Account conta = bankService.buscarConta(cpf);
            if (conta == null) {
                res.status(404);
                return gson.toJson(Map.of("erro", "Conta não encontrada."));
            }

            if (conta.getCartaoCredito() == null) {
                res.status(404);
                return gson.toJson(Map.of("erro", "Cartão de crédito não encontrado."));
            }

            CartaoCredito cartao = conta.getCartaoCredito();
            Fatura fatura = cartao.getFaturaAtual();

            double limiteTotal = cartao.getLimite();
            double valorFatura = fatura != null ? fatura.getTotal() : 0.0;
            double limiteDisponivel = limiteTotal - valorFatura;

            return gson.toJson(Map.of(
                    "numeroCartao", cartao.getNumero(),
                    "limiteTotal", limiteTotal,
                    "limiteDisponivel", limiteDisponivel,
                    "valorFatura", valorFatura,
                    "compras", fatura != null ? fatura.getCompras() : List.of()
            ));
        });
        
        //consultar informações do cartão de débito
        
        get("/api/contas/:cpf/cartoes/debito/info", (req, res) -> {
    res.type("application/json");
    String cpf = req.params("cpf");

    Account conta = bankService.buscarConta(cpf);
    if (conta == null) {
        res.status(404);
        return gson.toJson(Map.of("erro", "Conta não encontrada."));
    }

    if (conta.getCartaoDebito() == null) {
        res.status(404);
        return gson.toJson(Map.of("erro", "Cartão de débito não encontrado."));
    }

    CartaoDebito cartao = conta.getCartaoDebito();

    return gson.toJson(Map.of(
        "numeroCartao", cartao.getNumero(),
        "validade", cartao.getValidade(),
        "cvv", cartao.getCvv()
    ));
});



        
        //investir na poupança
        
        post("/api/contas/:cpf/poupanca/investir", (req, res) -> {
            res.type("application/json");
            String cpf = req.params("cpf");
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            if (body == null || !body.containsKey("valor")) {
                res.status(400);
                return gson.toJson(Map.of("erro", "Campo 'valor' é obrigatório."));
            }
            double valor = ((Number) body.get("valor")).doubleValue();
            try {
                Account conta = bankService.buscarConta(cpf);
                    if (conta instanceof ContaPoupanca poupanca) {
                        poupanca.investir(valor);
                        bankService.salvar();
                    } else {
                        throw new Exception("Conta não é do tipo poupança.");
                    }

                return gson.toJson(Map.of("mensagem", "Investimento realizado com sucesso."));
            } catch (Exception e) {
                res.status(400);
                return gson.toJson(Map.of("erro", e.getMessage()));
            }
        });

        
        //resgata da poupança
        
        post("/api/contas/:cpf/poupanca/resgatar", (req, res) -> {
            res.type("application/json");
            String cpf = req.params("cpf");
            @SuppressWarnings("unchecked")
            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            if (body == null || !body.containsKey("valor")) {
                res.status(400);
                return gson.toJson(Map.of("erro", "Campo 'valor' é obrigatório."));
            }
            double valor = ((Number) body.get("valor")).doubleValue();
            try {
                Account conta = bankService.buscarConta(cpf);
                    if (conta instanceof ContaPoupanca poupanca) {
                        poupanca.resgatar(valor);
                        bankService.salvar();
                    } else {
                        throw new Exception("Conta não é do tipo poupança.");
                    }

                return gson.toJson(Map.of("mensagem", "Resgate realizado com sucesso."));
            } catch (Exception e) {
                res.status(400);
                return gson.toJson(Map.of("erro", e.getMessage()));
            }
        });

        
        //cancela a conta
        
        put("/api/contas/:cpf/encerrar", (req, res) -> {
            res.type("application/json");
            String cpf = req.params("cpf");
            try {
                bankService.encerrarConta(cpf);
                return gson.toJson(Map.of("mensagem", "Conta encerrada com sucesso!"));
            } catch (Exception e) {
                res.status(400);
                return gson.toJson(Map.of("erro", e.getMessage()));
            }
        });

        System.out.println("🚀 Servidor iniciado em: http://localhost:8080/api/status");
    }
}
