import { useState } from "react";
import { useNavigate } from "react-router-dom";

export default function Saque() {
  const [valor, setValor] = useState("");
  const usuario = JSON.parse(localStorage.getItem("usuario"));
  const navigate = useNavigate();

  const handleSaque = async () => {
    if (!valor || valor <= 0) {
      alert("Informe um valor válido!");
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:8080/api/contas/${usuario.cpf}/saque`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ valor: parseFloat(valor) }),
        }
      );

      if (response.ok) {
        const data = await response.json();
        alert(data.mensagem || data);
        navigate("/menu");
      } else {
        const erro = await response.json();
        alert(erro.erro || erro);
      }
    } catch (error) {
      console.error(error);
      alert("Erro ao conectar com o servidor.");
    }
  };

  return (
    <div className="container">
      <h1>Saque</h1>
      <p>Conta: {usuario?.cpf}</p>
      <input
        type="number"
        value={valor}
        onChange={(e) => setValor(e.target.value)}
        placeholder="Digite o valor do saque"
      />
      <div className="actions">
        <button onClick={handleSaque}>Confirmar Saque</button>
        <button onClick={() => navigate("/menu")}>Cancelar</button>
      </div>
    </div>
  );
}
