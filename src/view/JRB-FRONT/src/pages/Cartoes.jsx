import { useEffect, useState } from "react";
import {
  compraCredito,
  compraDebito,
  pagarFatura,
  obterInfoCartaoCredito,
  baixarFaturaPDF,
  obterInfoCartaoDebito,
  solicitarCartaoCredito,
  solicitarCartaoDebito,
} from "../utils/api";

export default function Cartoes() {
  const usuario = JSON.parse(localStorage.getItem("usuario"));
  const cpf = usuario?.cpf;
  const tipoConta = usuario?.tipo?.toLowerCase(); // "corrente" ou "poupanca"

  const [valor, setValor] = useState("");
  const [descricao, setDescricao] = useState("");
  const [mensagem, setMensagem] = useState("");
  const [infoCartao, setInfoCartao] = useState(null);
  const [temCredito, setTemCredito] = useState(false);
  const [temDebito, setTemDebito] = useState(false);
  const [carregando, setCarregando] = useState(true);

  //puxa as informações do cartão
  useEffect(() => {
  if (!cpf) return;

  // 🔸 Contas poupança não têm cartão de crédito
  if (tipoConta === "poupanca") {
    setTemCredito(false);
    setCarregando(false);
    return;
  }

  const carregar = async () => {
    setCarregando(true);
    try {
      const dados = await obterInfoCartaoCredito(cpf);
      if (dados && dados.numeroCartao) {
        setInfoCartao(dados);
        setTemCredito(true);
      } else {
        setTemCredito(false);
      }
    } catch {
      setTemCredito(false);
    }
    setCarregando(false);
  };
  carregar();
}, [cpf, tipoConta]);

//puxa as informações do cartão de débito

useEffect(() => {
  if (!cpf) return;

  const carregarDebito = async () => {
    try {
      const dadosDebito = await obterInfoCartaoDebito(cpf);
      if (dadosDebito && dadosDebito.numeroCartao) {
        setTemDebito(true);
        setInfoCartao((prev) => ({ ...prev, debito: dadosDebito }));
      } else {
        setTemDebito(false);
      }
    } catch {
      setTemDebito(false);
    }
  };

  carregarDebito();
}, [cpf]);



  if (!cpf) {
    return (
      <div style={{ color: "white", textAlign: "center", marginTop: "80px" }}>
        <p>Faça login primeiro.</p>
        <a href="/" style={{ color: "#6f6fff" }}>Ir ao Login</a>
      </div>
    );
  }

  //solicitar Cartão de Crédito
  const handleSolicitarCredito = async (limite) => {
    if (tipoConta === "poupanca") {
      setMensagem("Contas poupança não podem solicitar cartão de crédito.");
      return;
    }

    try {
      const resp = await solicitarCartaoCredito(cpf, limite);
      setMensagem(resp.mensagem || "Cartão de crédito solicitado com sucesso!");
      setTemCredito(true);
      const dados = await obterInfoCartaoCredito(cpf);
      setInfoCartao(dados);
    } catch (e) {
      setMensagem("Erro ao solicitar cartão de crédito: " + e.message);
    }
  };

  //solicitar Cartão de Débito
  const handleSolicitarDebito = async () => {
    try {
      const resp = await solicitarCartaoDebito(cpf);
      setMensagem(resp.mensagem || "Cartão de débito solicitado com sucesso!");
      setTemDebito(true);
    } catch (e) {
      setMensagem("Erro ao solicitar cartão de débito: " + e.message);
    }
  };

  //compra Crédito
  const handleCompraCredito = async () => {
    try {
      const resp = await compraCredito(cpf, parseFloat(valor), descricao);
      setMensagem(resp.mensagem || resp.erro);
      setValor("");
      setDescricao("");
      const atualizadas = await obterInfoCartaoCredito(cpf);
      setInfoCartao(atualizadas);
    } catch (e) {
      setMensagem("Erro na compra crédito: " + e.message);
    }
  };

  //compra Débito
  const handleCompraDebito = async () => {
    try {
      const resp = await compraDebito(cpf, parseFloat(valor), descricao);
      setMensagem(resp.mensagem || resp.erro);
      setValor("");
      setDescricao("");
    } catch (e) {
      setMensagem("Erro na compra débito: " + e.message);
    }
  };

  //pagar Fatura
  const handlePagarFatura = async () => {
    try {
      const resp = await pagarFatura(cpf);
      setMensagem(resp.mensagem || resp.erro);
      const atualizadas = await obterInfoCartaoCredito(cpf);
      setInfoCartao(atualizadas);
    } catch (e) {
      setMensagem("Erro ao pagar fatura: " + e.message);
    }
  };

  //baixar Fatura PDF
  const handleBaixarPDF = () => {
    if (infoCartao?.numeroCartao) {
      baixarFaturaPDF(cpf, infoCartao.numeroCartao);
    } else {
      setMensagem("Cartão não encontrado.");
    }
  };

  return (
    <div style={{ color: "white", textAlign: "center", marginTop: "50px" }}>
      <h2>💳 Cartões</h2>

      {tipoConta === "poupanca" && (
        <p style={{ color: "yellow", marginBottom: "20px" }}>
          ⚠️ Contas poupança não podem possuir cartão de crédito.
        </p>
      )}

      
      {(!temCredito || !temDebito) && (
        <div style={{ marginTop: "30px" }}>
          <p>Você ainda pode solicitar novos cartões:</p>

          {/* Cartão de credito  só para contas correntes */}
          {tipoConta !== "poupanca" && !temCredito && (
            <div style={{ marginBottom: "25px" }}>
              <h4>🪪 Solicitar Cartão de Crédito</h4>
              <input
                type="number"
                id="limiteCartao"
                min="100"
                step="100"
                placeholder="Limite desejado (R$)"
                style={{
                  padding: "5px",
                  borderRadius: "6px",
                  border: "1px solid gray",
                  width: "150px",
                  marginBottom: "10px",
                }}
              />
              <br />
              <button
                onClick={() => {
                  const limite = parseFloat(document.getElementById("limiteCartao").value);
                  if (isNaN(limite) || limite <= 0) {
                    setMensagem("Informe um limite válido antes de solicitar o cartão.");
                    return;
                  }
                  handleSolicitarCredito(limite);
                }}
                style={{
                  backgroundColor: "#4caf50",
                  color: "white",
                  padding: "10px 20px",
                  border: "none",
                  borderRadius: "8px",
                  cursor: "pointer",
                }}
              >
                Solicitar Cartão de Crédito
              </button>
            </div>
          )}

          {/* Cartão de débito para todos */}
          {!temDebito && (
            <div style={{ marginBottom: "20px" }}>
              <h4>💳 Solicitar Cartão de Débito</h4>
              <button
                onClick={handleSolicitarDebito}
                style={{
                  backgroundColor: "#2196f3",
                  color: "white",
                  padding: "10px 20px",
                  border: "none",
                  borderRadius: "8px",
                  cursor: "pointer",
                }}
              >
                Solicitar Cartão de Débito
              </button>
            </div>
          )}
        </div>
      )}

      
      {carregando ? (
        <p>Carregando informações do cartão...</p>
      ) : temCredito && infoCartao ? (
        <div style={{ marginTop: "20px" }}>
          <h3>💳 Cartão de Crédito</h3>
          <p><strong>Número:</strong> {infoCartao.numeroCartao}</p>
          <p><strong>Limite Total:</strong> R$ {infoCartao.limiteTotal.toFixed(2)}</p>
          <p><strong>Limite Disponível:</strong> R$ {infoCartao.limiteDisponivel.toFixed(2)}</p>
          <p><strong>Fatura Atual:</strong> R$ {infoCartao.valorFatura.toFixed(2)}</p>

          <button onClick={handleBaixarPDF}>📄 Baixar Fatura PDF</button>

          <h3 style={{ marginTop: "30px" }}>🧾 Compras da Fatura</h3>
          {infoCartao.compras?.length > 0 ? (
            <table
              style={{
                margin: "20px auto",
                borderCollapse: "collapse",
                color: "white",
                width: "70%",
              }}
            >
              <thead>
                <tr style={{ borderBottom: "2px solid white" }}>
                  <th>Data</th>
                  <th>Descrição</th>
                  <th>Valor (R$)</th>
                </tr>
              </thead>
              <tbody>
                {infoCartao.compras.map((c, i) => (
                  <tr key={i} style={{ borderBottom: "1px solid gray" }}>
                    <td>{c.dataHora || c.data || "-"}</td>
                    <td>{c.descricao || c.tipo || "-"}</td>
                    <td>R$ {Number(c.valor).toFixed(2)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p>Nenhuma compra na fatura atual.</p>
          )}

          <h3 style={{ marginTop: "40px" }}>💰 Realizar Compra</h3>
          <input
            type="text"
            placeholder="Descrição"
            value={descricao}
            onChange={(e) => setDescricao(e.target.value)}
            style={{ marginRight: "10px" }}
          />
          <input
            type="number"
            placeholder="Valor"
            value={valor}
            onChange={(e) => setValor(e.target.value)}
            style={{ marginRight: "10px" }}
          />
          <button onClick={handleCompraCredito}>Compra Crédito</button>
          <button onClick={handleCompraDebito} style={{ marginLeft: "10px" }}>
            Compra Débito
          </button>

          <div style={{ marginTop: "30px" }}>
            <button onClick={handlePagarFatura}>💸 Pagar Fatura</button>
          </div>
        </div>
      ) : null}
      
{temDebito && infoCartao?.debito && (
  <div style={{ marginTop: "40px" }}>
    <h3>💳 Cartão de Débito</h3>
    <p><strong>Número:</strong> {infoCartao.debito.numeroCartao}</p>

    <p>Você pode realizar compras diretamente com o saldo da conta.</p>

    <input
      type="text"
      placeholder="Descrição"
      value={descricao}
      onChange={(e) => setDescricao(e.target.value)}
      style={{ marginRight: "10px" }}
    />
    <input
      type="number"
      placeholder="Valor"
      value={valor}
      onChange={(e) => setValor(e.target.value)}
      style={{ marginRight: "10px" }}
    />
    <button onClick={handleCompraDebito}>Compra Débito</button>
  </div>
)}


      {mensagem && <p style={{ marginTop: "20px", color: "lightgreen" }}>{mensagem}</p>}

      <a href="/menu" style={{ color: "#6f6fff", display: "block", marginTop: "30px" }}>
        Voltar
      </a>
    </div>
  );
}
