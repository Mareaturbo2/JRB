import { useState } from "react";
import { transferir } from "../utils/api";
import { Link } from "react-router-dom";

export default function Transferencia() {
  //olha o CPF salvo no localStorage após o login
  const usuario = JSON.parse(localStorage.getItem("usuario"));
  const cpfOrigem = usuario?.cpf;

  const [cpfDestino, setCpfDestino] = useState("");
  const [valor, setValor] = useState("");
  const [mensagem, setMensagem] = useState("");
  const [erro, setErro] = useState("");

  const handleTransferir = async (e) => {
    e.preventDefault();
    setMensagem("");
    setErro("");

    try {
      const resposta = await transferir(cpfOrigem, cpfDestino, parseFloat(valor));
      if (resposta.erro) {
        setErro("❌ " + resposta.erro);
      } else {
        setMensagem("✅ " + (resposta.mensagem || JSON.stringify(resposta)));
      }
    } catch (err) {
      setErro("❌ Erro na transferência: " + err.message);
    }
  };

  if (!cpfOrigem) {
    return <p>⚠️ Faça login antes de realizar transferências.</p>;
  }

  return (
    <div className="card">
      <h2>Transferência</h2>
      <form onSubmit={handleTransferir} className="form">
        <input
          type="text"
          placeholder="CPF Destino"
          value={cpfDestino}
          onChange={(e) => setCpfDestino(e.target.value)}
          required
        />
        <input
          type="number"
          step="0.01"
          placeholder="Valor"
          value={valor}
          onChange={(e) => setValor(e.target.value)}
          required
        />
        <button type="submit">Transferir</button>
      </form>

      {mensagem && <p className="ok">{mensagem}</p>}
      {erro && <p className="err">{erro}</p>}

      <Link to="/menu">Voltar</Link>
    </div>
  );
}
