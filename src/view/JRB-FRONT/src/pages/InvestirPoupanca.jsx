import { useEffect, useState } from "react";
import { detalhesConta, investirPoupanca } from "../utils/api";
import { getCpfLogado } from "../utils/auth";
import { useNavigate } from "react-router-dom";

export default function InvestirPoupanca() {
  const [saldo, setSaldo] = useState(0);
  const [investimentoAtual, setInvestimentoAtual] = useState(0);
  const [valor, setValor] = useState("");
  const [mensagem, setMensagem] = useState("");
  const cpf = getCpfLogado();
  const navigate = useNavigate();

  useEffect(() => {
    async function carregarDados() {
      try {
        const dados = await detalhesConta(cpf);
        setSaldo(dados.saldo || 0);

        //verifica os investimentos dentro da poupança
        if (typeof dados.investimento === "number") {
  setInvestimentoAtual(dados.investimento);
} else if (dados.poupanca && typeof dados.poupanca.investimento === "number") {
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

  async function investir() {
    setMensagem("");
    if (!valor || isNaN(valor) || Number(valor) <= 0) {
      setMensagem("Informe um valor válido para investir.");
      return;
    }
    if (Number(valor) > saldo) {
      setMensagem("Saldo insuficiente para investir.");
      return;
    }

    try {
      const resp = await investirPoupanca(cpf, Number(valor));
      setMensagem(resp.mensagem || "Investimento realizado!");
      setValor("");
      //atualiza saldo e investimento
      const novosDados = await detalhesConta(cpf);
      setSaldo(novosDados.saldo || 0);
      setInvestimentoAtual(
  novosDados.investimento || novosDados.poupanca?.investimento || 0
);

    } catch (e) {
      setMensagem(e.message || "Erro ao investir na poupança.");
    }
  }

  return (
    <div className="text-center mt-10">
      <h1 className="text-4xl font-bold text-white mb-6">
        💰 Investir na Poupança
      </h1>

      <p className="text-lg text-white">
        <strong>Saldo disponível:</strong> R$ {saldo.toFixed(2)}
      </p>
      <p className="text-lg text-white mb-4">
        <strong>Investimento atual:</strong> R$ {investimentoAtual.toFixed(2)}
      </p>

      <div className="mt-4">
        <input
          type="number"
          placeholder="Valor para investir"
          value={valor}
          onChange={(e) => setValor(e.target.value)}
          className="p-2 rounded bg-gray-800 text-white text-center"
        />
        <button
          onClick={investir}
          className="ml-3 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded"
        >
          Investir
        </button>
      </div>

      {mensagem && (
        <p className="mt-4 text-green-400 font-semibold">{mensagem}</p>
      )}

      <button
        onClick={() => navigate("/menu")}
        className="mt-6 px-4 py-2 bg-gray-700 hover:bg-gray-800 text-white rounded"
      >
        ← Voltar ao Menu
      </button>
    </div>
  );
}
