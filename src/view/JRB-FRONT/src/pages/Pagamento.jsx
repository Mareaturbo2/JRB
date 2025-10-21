import React, { useState } from "react";
import { Link } from "react-router-dom";
import { pagarBoleto } from "../utils/api"; 

export default function Pagamento() {
  const usuario = JSON.parse(localStorage.getItem("usuario"));
  const cpf = usuario?.cpf;

  const [codigo, setCodigo] = useState("");
  const [valor, setValor] = useState("");
  const [dataVencimento, setDataVencimento] = useState("");
  const [mensagem, setMensagem] = useState("");

  const handlePagamento = async () => {
    if (!codigo || !valor) {
      setMensagem("❌ Informe o código e o valor do boleto.");
      return;
    }

    try {
      const data = await pagarBoleto(cpf, codigo, parseFloat(valor), dataVencimento);

      if (data.mensagem) {
        setMensagem("✅ " + data.mensagem);
      } else {
        setMensagem("❌ " + (data.erro || "Erro desconhecido."));
      }
    } catch (erro) {
      setMensagem("❌ Erro de conexão: " + erro.message);
    }
  };

  return (
    <div style={{ padding: "30px", textAlign: "center", color: "white" }}>
      <h1>Pagamento de Conta / Boleto</h1>

      <div style={{ marginTop: "20px" }}>
        <label>
          Número do Boleto:
          <input
            type="text"
            value={codigo}
            onChange={(e) => setCodigo(e.target.value)}
            style={{ margin: "5px", padding: "5px" }}
          />
        </label>

        <label>
          Valor (R$):
          <input
            type="number"
            value={valor}
            onChange={(e) => setValor(e.target.value)}
            style={{ margin: "5px", padding: "5px" }}
          />
        </label>

        <label>
          Vencimento (opcional):
          <input
            type="text"
            placeholder="DD/MM/AAAA"
            value={dataVencimento}
            onChange={(e) => setDataVencimento(e.target.value)}
            style={{ margin: "5px", padding: "5px" }}
          />
        </label>

        <button
          onClick={handlePagamento}
          style={{
            marginLeft: "10px",
            padding: "6px 16px",
            backgroundColor: "#222",
            color: "white",
            border: "1px solid white",
            borderRadius: "6px",
            cursor: "pointer",
          }}
        >
          Pagar
        </button>
      </div>

      {mensagem && (
        <p
          style={{
            marginTop: "20px",
            color: mensagem.startsWith("✅") ? "lightgreen" : "salmon",
          }}
        >
          {mensagem}
        </p>
      )}

      <div style={{ marginTop: "20px" }}>
        <Link to="/menu" style={{ color: "lightblue", textDecoration: "none" }}>
          Voltar
        </Link>
      </div>
    </div>
  );
}
