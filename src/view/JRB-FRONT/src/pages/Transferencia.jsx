import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { transferir, getCpfLogado } from "../utils/api";
import "../App.css";

export default function Transferencia() {
  const navigate = useNavigate();
  const cpfOrigem = getCpfLogado();
  const [cpfDestino, setCpfDestino] = useState("");
  const [valor, setValor] = useState("");
  const [mensagem, setMensagem] = useState("");

  const handleTransferir = async () => {
    if (!cpfDestino || !valor || valor <= 0) {
      setMensagem("Preencha todos os campos corretamente!");
      return;
    }

    try {
      const res = await transferir(cpfOrigem, cpfDestino, parseFloat(valor));
      setMensagem(res.mensagem || "Transferência realizada com sucesso!");
      setCpfDestino("");
      setValor("");
    } catch (e) {
      setMensagem("Erro: " + e.message);
    }
  };

  return (
    <div className="page">
      <div className="card">
        <h2>Transferência</h2>
        <p>Informe o CPF de destino e o valor:</p>
        <input
          type="text"
          placeholder="CPF Destino"
          value={cpfDestino}
          onChange={(e) => setCpfDestino(e.target.value)}
        />
        <input
          type="number"
          placeholder="Valor (R$)"
          value={valor}
          onChange={(e) => setValor(e.target.value)}
        />
        <button className="btn cadastro" onClick={handleTransferir}>
          Confirmar Transferência
        </button>
        <button className="btn login" onClick={() => navigate("/menu")}>
          Voltar ao Menu
        </button>
        {mensagem && <p>{mensagem}</p>}
      </div>
    </div>
  );
}
