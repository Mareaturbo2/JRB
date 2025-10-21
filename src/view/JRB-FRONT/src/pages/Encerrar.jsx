import { useState } from "react";
import { getCpfLogado, encerrarConta } from "../utils/api";

export default function Encerrar() {
  const [mensagem, setMensagem] = useState("");
 const usuario = JSON.parse(localStorage.getItem("usuario")) || {};
const cpf = usuario.cpf;


  const handleEncerrar = async () => {
    try {
      const resp = await encerrarConta(cpf);
      setMensagem(resp.mensagem || "Conta encerrada com sucesso.");
      localStorage.clear(); // limpa login
    } catch (erro) {
      setMensagem("Erro ao encerrar: " + erro.message);
    }
  };

  return (
    <div style={{ color: "white", textAlign: "center", marginTop: "100px" }}>
      <h2>Encerrar Conta</h2>
      <p>Deseja realmente encerrar sua conta?</p>
      <button onClick={handleEncerrar} style={{ backgroundColor: "red", color: "white" }}>
        Confirmar Encerramento
      </button>
      <p>{mensagem}</p>
      <a href="/menu" style={{ color: "#6f6fff" }}>Voltar</a>
    </div>
  );
}
