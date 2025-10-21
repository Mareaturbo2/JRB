import { useState } from "react";
import { getCpfLogado, depositar } from "../utils/api";

export default function Deposito() {
  const [valor, setValor] = useState("");
  const [mensagem, setMensagem] = useState("");
 const usuario = JSON.parse(localStorage.getItem("usuario")) || {};
  const cpf = usuario.cpf;


  const handleDeposito = async () => {
    try {
      const resp = await depositar(cpf, parseFloat(valor));
      setMensagem(resp.mensagem || "Depósito realizado com sucesso!");
      setValor("");
    } catch (erro) {
      setMensagem("Erro no depósito: " + erro.message);
    }
  };

  return (
    <div style={{ color: "white", textAlign: "center", marginTop: "100px" }}>
      <h2>Depósito</h2>
      <input
        type="number"
        placeholder="Valor (R$)"
        value={valor}
        onChange={(e) => setValor(e.target.value)}
        style={{ marginRight: "10px" }}
      />
      <button onClick={handleDeposito}>Depositar</button>
      <p>{mensagem}</p>
      <a href="/menu" style={{ color: "#6f6fff" }}>Voltar</a>
    </div>
  );
}
