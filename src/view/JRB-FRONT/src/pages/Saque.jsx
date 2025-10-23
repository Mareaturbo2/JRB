import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../App.css";

export default function Saque() {
  const navigate = useNavigate();
  const usuario = JSON.parse(localStorage.getItem("usuario"));
  const [valor, setValor] = useState("");
  const [mensagem, setMensagem] = useState("");

  const handleSaque = async () => {
    if (!valor || valor <= 0) {
      setMensagem("Informe um valor válido!");
      return;
    }

    try {
      const response = await fetch(`http://localhost:8080/api/contas/${usuario.cpf}/saque`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ valor: parseFloat(valor) }),
      });

      if (response.ok) {
        const data = await response.text();
        setMensagem(data || "Saque realizado com sucesso!");
        setValor("");
      } else {
        const erro = await response.text();
        setMensagem(erro);
      }
    } catch (e) {
      setMensagem("Erro ao conectar com o servidor.");
    }
  };

  return (
    <div className="page">
      <div className="card">
        <h2>Saque</h2>
        <p>Informe o valor que deseja sacar:</p>
        <input
          type="number"
          placeholder="Ex: 50.00"
          value={valor}
          onChange={(e) => setValor(e.target.value)}
        />
        <button className="btn cadastro" onClick={handleSaque}>
          Confirmar Saque
        </button>
        <button className="btn login" onClick={() => navigate("/menu")}>
          Voltar ao Menu
        </button>
        {mensagem && <p>{mensagem}</p>}
      </div>
    </div>
  );
}
