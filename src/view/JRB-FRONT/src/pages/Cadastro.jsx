import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { criarConta } from "../utils/api";
import "../App.css";

export default function Cadastro() {
  const navigate = useNavigate();
  const [dados, setDados] = useState({
    titular: "",
    cpf: "",
    senha: "",
    tipo: "corrente",
    saldoInicial: 0,
  });
  const [mensagem, setMensagem] = useState("");

  const handleChange = (e) => {
    setDados({ ...dados, [e.target.name]: e.target.value });
  };

  const handleCadastro = async () => {
    try {
      const res = await criarConta({
        ...dados,
        saldoInicial: parseFloat(dados.saldoInicial) || 0,
      });
      setMensagem(res.mensagem || "Conta criada com sucesso!");
      setTimeout(() => navigate("/login"), 1500);
    } catch (e) {
      setMensagem("Erro: " + e.message);
    }
  };

  return (
    <div className="page">
      <div className="card">
        <h2>Cadastro</h2>
        <p>Crie sua conta</p>
        <div className="form">
          <input
            name="titular"
            placeholder="Nome completo"
            value={dados.titular}
            onChange={handleChange}
          />
          <input
            name="cpf"
            placeholder="CPF"
            value={dados.cpf}
            onChange={handleChange}
          />
          <input
            name="senha"
            placeholder="Senha"
            type="password"
            value={dados.senha}
            onChange={handleChange}
          />
          <select name="tipo" value={dados.tipo} onChange={handleChange}>
            <option value="corrente">Conta Corrente</option>
            <option value="poupanca">Conta Poupança</option>
          </select>
          <input
            name="saldoInicial"
            type="number"
            step="0.01"
            placeholder="Saldo inicial"
            value={dados.saldoInicial}
            onChange={handleChange}
          />
          <button className="btn cadastro" onClick={handleCadastro}>
            Criar Conta
          </button>
          <button className="btn login" onClick={() => navigate("/")}>
            Voltar
          </button>
          {mensagem && <p>{mensagem}</p>}
        </div>
      </div>
    </div>
  );
}
