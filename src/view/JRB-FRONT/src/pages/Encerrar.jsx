import { useNavigate } from "react-router-dom";
import { encerrarConta, getCpfLogado } from "../utils/api";
import "../App.css";
import { useState } from "react";


export default function Encerrar() {
  const navigate = useNavigate();
  const cpf = getCpfLogado();
  const [mensagem, setMensagem] = useState("");

  const handleEncerrar = async () => {
    if (!confirm("Deseja realmente encerrar sua conta?")) return;

    try {
      const msg = await encerrarConta(cpf);
      setMensagem(msg);
      localStorage.clear();
      setTimeout(() => navigate("/"), 2000);
    } catch (e) {
      setMensagem("Erro: " + e.message);
    }
  };

  return (
    <div className="page">
      <div className="card">
        <h2>Encerrar Conta</h2>
        <p>Tem certeza que deseja encerrar sua conta?</p>
        <button className="btn cadastro" onClick={handleEncerrar}>
          Confirmar Encerramento
        </button>
        {mensagem && <p>{mensagem}</p>}
        <button className="btn login" onClick={() => navigate("/menu")}>
          Voltar
        </button>
      </div>
    </div>
  );
}
