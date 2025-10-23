import { useEffect, useState } from "react";
import { detalhesConta, investirPoupanca } from "../utils/api";
import { useNavigate } from "react-router-dom";
import "../App.css";

export default function InvestirPoupanca() {
  const [saldo, setSaldo] = useState(0);
  const [investimentoAtual, setInvestimentoAtual] = useState(0);
  const [valor, setValor] = useState("");
  const [mensagem, setMensagem] = useState("");
  const usuario = JSON.parse(localStorage.getItem("usuario")) || {};
  const cpf = usuario.cpf;
  const navigate = useNavigate();

  //bloqueia acesso por URL para quem não for conta poupança
  useEffect(() => {
    const tipoNormalizado = String(usuario.tipo || "")
      .normalize("NFD")
      .replace(/\p{Diacritic}/gu, "")
      .toLowerCase();
    if (!tipoNormalizado.includes("poupanca")) {
      navigate("/menu", { replace: true });
    }
  }, [usuario, navigate]);

  //carrega saldo e investimento atual
  useEffect(() => {
    async function carregarDados() {
      try {
        const dados = await detalhesConta(cpf);
        setSaldo(dados.saldo || 0);

        // Detecta investimento dentro de `poupanca` ou direto
        if (typeof dados.investimento === "number") {
          setInvestimentoAtual(dados.investimento);
        } else if (dados.poupanca?.investimento) {
          setInvestimentoAtual(dados.poupanca.investimento);
        } else {
          setInvestimentoAtual(0);
        }
      } catch (e) {
        console.error(e);
        setMensagem("Erro ao carregar dados da conta.");
      }
    }
    carregarDados();
  }, [cpf]);

  //investir
  async function investir() {
    setMensagem("");
    const valorNum = parseFloat(valor);
    if (!valorNum || valorNum <= 0) {
      setMensagem("Informe um valor válido para investir.");
      return;
    }
    if (valorNum > saldo) {
      setMensagem("Saldo insuficiente para investir.");
      return;
    }

    try {
      const resp = await investirPoupanca(cpf, valorNum);
      setMensagem(resp.mensagem || "Investimento realizado com sucesso!");
      setValor("");

      //atualiza valores apos investir
      const novosDados = await detalhesConta(cpf);
      setSaldo(novosDados.saldo || 0);
      setInvestimentoAtual(
        novosDados.investimento ||
          novosDados.poupanca?.investimento ||
          0
      );
    } catch (e) {
      setMensagem("Erro ao realizar investimento.");
      console.error(e);
    }
  }

  return (
    <div className="page">
      <div className="card" style={{ width: 340 }}>
        <h2>Investir na Poupança 💰</h2>

        <p style={{ color: "gray", marginBottom: 10 }}>
          Saldo disponível: <b>R$ {saldo.toFixed(2)}</b>
        </p>
        <p style={{ color: "gray", marginBottom: 20 }}>
          Investimento atual: <b>R$ {investimentoAtual.toFixed(2)}</b>
        </p>

        <div className="form" style={{ gap: 10 }}>
          <input
            type="number"
            placeholder="Valor a investir"
            value={valor}
            onChange={(e) => setValor(e.target.value)}
            className="input"
          />

          <button className="btn cadastro" onClick={investir}>
            Confirmar Investimento
          </button>
        </div>

        {mensagem && (
          <p
            style={{
              marginTop: 10,
              color: mensagem.includes("sucesso") ? "green" : "red",
            }}
          >
            {mensagem}
          </p>
        )}

        <button
          className="btn login"
          style={{ marginTop: 20 }}
          onClick={() => navigate("/menu")}
        >
          Voltar ao Menu
        </button>
      </div>
    </div>
  );
}
