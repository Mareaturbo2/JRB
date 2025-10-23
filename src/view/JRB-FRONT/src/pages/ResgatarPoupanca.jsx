import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { resgatarPoupanca, detalhesConta } from "../utils/api";
import "../App.css";

export default function ResgatarPoupanca() {
  const navigate = useNavigate();
  const usuario = JSON.parse(localStorage.getItem("usuario")) || {};
  const cpf = usuario.cpf;

  const [valor, setValor] = useState("");
  const [mensagem, setMensagem] = useState("");
  const [disponivel, setDisponivel] = useState(0.0);
  const [carregando, setCarregando] = useState(true);

  //impede acesso direto se não for conta poupança
  useEffect(() => {
    const tipoNormalizado = String(usuario.tipo || "")
      .normalize("NFD")
      .replace(/\p{Diacritic}/gu, "")
      .toLowerCase();
    if (!tipoNormalizado.includes("poupanca")) {
      navigate("/menu", { replace: true });
    }
  }, [usuario, navigate]);

  //busca valor investido atual
  useEffect(() => {
    async function carregarInvestimento() {
      try {
        const conta = await detalhesConta(cpf);
        const investimento =
          conta.poupanca?.investimento ??
          conta.investimento ??
          0;
        setDisponivel(parseFloat(investimento.toFixed(2)));
      } catch (e) {
        console.error("Erro ao buscar saldo da poupança:", e);
      } finally {
        setCarregando(false);
      }
    }
    carregarInvestimento();
  }, [cpf]);

  //resgatar
  const handleResgatar = async (e) => {
    e.preventDefault();
    setMensagem("");

    const valorNum = parseFloat(valor);
    if (!valorNum || valorNum <= 0) {
      setMensagem("Informe um valor válido para resgate.");
      return;
    }
    if (valorNum > disponivel) {
      setMensagem("Valor superior ao disponível para resgate!");
      return;
    }

    try {
      const resp = await resgatarPoupanca(cpf, valorNum);
      setMensagem(resp.mensagem || "Resgate realizado com sucesso!");
      setValor("");

      //atualiza saldo disponível
      const contaAtualizada = await detalhesConta(cpf);
      const novoInvest = contaAtualizada.poupanca?.investimento ?? contaAtualizada.investimento ?? 0;
      setDisponivel(parseFloat(novoInvest.toFixed(2)));
    } catch (e) {
      console.error(e);
      setMensagem("Erro ao realizar resgate. Tente novamente.");
    }
  };

  return (
    <div className="page">
      <div className="card" style={{ width: 340 }}>
        <h2>Resgatar da Poupança</h2>

        {carregando ? (
          <p>Carregando saldo disponível...</p>
        ) : (
          <p style={{ color: "gray", marginBottom: 10 }}>
            Disponível para resgate: <b>R$ {disponivel.toFixed(2)}</b>
          </p>
        )}

        <form className="form" onSubmit={handleResgatar}>
          <input
            type="number"
            step="0.01"
            placeholder="Valor a resgatar"
            value={valor}
            onChange={(e) => setValor(e.target.value)}
          />
          <button className="btn cadastro" type="submit">
            Confirmar Resgate
          </button>
        </form>

        {mensagem && (
          <p
            style={{
              color: mensagem.includes("sucesso") ? "green" : "red",
              marginTop: 10,
            }}
          >
            {mensagem}
          </p>
        )}

        <button
          className="btn login"
          style={{ marginTop: 15 }}
          onClick={() => navigate("/menu")}
        >
          Voltar ao Menu
        </button>
      </div>
    </div>
  );
}
