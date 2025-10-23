import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { pagarBoleto } from "../utils/api";
import "../App.css";

export default function Pagamento() {
  const navigate = useNavigate();
  const usuario = JSON.parse(localStorage.getItem("usuario")) || {};
  const [codigo, setCodigo] = useState("");
  const [valor, setValor] = useState("");
  const [mensagem, setMensagem] = useState("");
  const [erro, setErro] = useState("");

  async function handlePagamento(e) {
    e.preventDefault();
    setErro("");
    setMensagem("");

    try {
      //faz o pagamento via api
      const resposta = await pagarBoleto(usuario.cpf, codigo, parseFloat(valor));

      //mostra mensagem de sucesso
      setMensagem(resposta.mensagem || "Pagamento realizado com sucesso!");

      //nome esperado do arquivo igual ao gerado no backend
      const nomeArquivo = `comprovante_boleto_${codigo}.pdf`;
      const urlArquivo = `http://localhost:8080/${nomeArquivo}`;

      //baixa automaticamente o pdf gerado
      setTimeout(() => {
        const link = document.createElement("a");
        link.href = urlArquivo;
        link.download = nomeArquivo;
        link.target = "_blank";
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      }, 1500);

    } catch (err) {
      console.error(err);
      setErro("Erro ao processar o pagamento: " + err.message);
    }
  }

  return (
    <div className="page">
      <div className="card" style={{ width: 360 }}>
        <h2>Pagamento de Boleto</h2>

        <form className="form" onSubmit={handlePagamento}>
          <input
            type="text"
            placeholder="Código do boleto"
            value={codigo}
            onChange={(e) => setCodigo(e.target.value)}
            className="input"
            required
          />

          <input
            type="number"
            placeholder="Valor"
            value={valor}
            onChange={(e) => setValor(e.target.value)}
            className="input"
            required
          />

          {erro && <p style={{ color: "red" }}>{erro}</p>}
          {mensagem && <p style={{ color: "green" }}>{mensagem}</p>}

          <button type="submit" className="btn cadastro">
            Pagar
          </button>
        </form>

        <button
          className="btn login"
          onClick={() => navigate("/menu")}
        >
          Voltar
        </button>
      </div>
    </div>
  );
}
