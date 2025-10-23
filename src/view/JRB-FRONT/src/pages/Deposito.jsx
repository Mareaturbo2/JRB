import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { depositar, getCpfLogado } from "../utils/api";
import "../App.css";

export default function Deposito() {
  const navigate = useNavigate();
  const cpf = getCpfLogado();
  const [valor, setValor] = useState("");
  const [mensagem, setMensagem] = useState("");

  const handleDeposito = async () => {
    if (!valor || valor <= 0) {
      setMensagem("Informe um valor válido!");
      return;
    }

    try {
      const resposta = await depositar(cpf, parseFloat(valor));
      setMensagem(resposta.mensagem || "Depósito realizado com sucesso!");
      setValor("");
    } catch (e) {
      setMensagem("Erro: " + e.message);
    }
  };

  return (
    <div className="page">
      <div className="card">
        <h2>Depósito</h2>
        <p>Informe o valor para depósito:</p>
        <input
          type="number"
          placeholder="Ex: 100.00"
          value={valor}
          onChange={(e) => setValor(e.target.value)}
        />
        <button className="btn cadastro" onClick={handleDeposito}>
          Confirmar Depósito
        </button>
        <button className="btn login" onClick={() => navigate("/menu")}>
          Voltar ao Menu
        </button>
        {mensagem && <p>{mensagem}</p>}
      </div>
    </div>
  );
}
