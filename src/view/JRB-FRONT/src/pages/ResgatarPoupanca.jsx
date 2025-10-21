import { useEffect, useState } from "react";
import { detalhesConta, resgatarPoupanca } from "../utils/api";
import { getCpfLogado } from "../utils/auth";
import { useNavigate } from "react-router-dom";

export default function ResgatarPoupanca() {
  const navigate = useNavigate();
  const [investimento, setInvestimento] = useState(0);
  const [valor, setValor] = useState("");
  const [mensagem, setMensagem] = useState("");
  const [erro, setErro] = useState("");
  const cpf = getCpfLogado();

  useEffect(() => {
    if (!cpf) {
      navigate("/login");
      return;
    }

    async function carregarInvestimento() {
      try {
        const dados = await detalhesConta(cpf);
        
        const invest =
          typeof dados.investimento === "number"
            ? dados.investimento
            : dados.poupanca?.investimento || 0;
        setInvestimento(invest);
      } catch (err) {
        setErro("Erro ao buscar informações da poupança.");
      }
    }

    carregarInvestimento();
  }, [cpf, navigate]);

  async function resgatar() {
    setErro("");
    setMensagem("");

    if (!valor || isNaN(valor) || valor <= 0) {
      setErro("Informe um valor válido para resgatar.");
      return;
    }

    if (Number(valor) > investimento) {
      setErro("Valor excede o total investido na poupança.");
      return;
    }

    try {
      const resp = await resgatarPoupanca(cpf, parseFloat(valor));
      setMensagem(resp.mensagem || "Resgate realizado com sucesso!");
      setValor("");

      //atualiza o valor investido após o resgate
      const dados = await detalhesConta(cpf);
      const investAtual =
        typeof dados.investimento === "number"
          ? dados.investimento
          : dados.poupanca?.investimento || 0;
      setInvestimento(investAtual);
    } catch (err) {
      setErro("Erro ao realizar resgate.");
    }
  }

  return (
    <div className="text-center mt-10">
      <h1 className="text-4xl font-bold text-white mb-6">
        🏦 Resgatar da Poupança
      </h1>

      <p className="text-lg text-white mb-2">
        <strong>Valor disponível para resgate:</strong> R$ {investimento.toFixed(2)}
      </p>

      <div className="mt-4">
        <input
          type="number"
          placeholder="Valor a resgatar"
          value={valor}
          onChange={(e) => setValor(e.target.value)}
          className="p-2 rounded bg-gray-800 text-white text-center"
        />
        <button
          onClick={resgatar}
          className="ml-3 px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded"
        >
          Resgatar
        </button>
      </div>

      {mensagem && <p className="mt-4 text-green-400 font-semibold">{mensagem}</p>}
      {erro && <p className="mt-4 text-red-400 font-semibold">{erro}</p>}

      <button
        onClick={() => navigate("/menu")}
        className="mt-6 px-4 py-2 bg-gray-700 hover:bg-gray-800 text-white rounded"
      >
        ← Voltar ao Menu
      </button>
    </div>
  );
}
