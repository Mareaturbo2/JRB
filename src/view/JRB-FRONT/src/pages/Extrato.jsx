import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../App.css";

export default function Extrato() {
  const navigate = useNavigate();
  const usuario = JSON.parse(localStorage.getItem("usuario"));

  const [dataInicio, setDataInicio] = useState("");
  const [dataFim, setDataFim] = useState("");
  const [movimentacoes, setMovimentacoes] = useState([]);
  const [mensagem, setMensagem] = useState("");

  const buscarExtrato = async () => {
    if (!dataInicio || !dataFim) {
      setMensagem("Informe as datas de início e fim.");
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:8080/api/contas/${usuario.cpf}/extrato?inicio=${dataInicio}&fim=${dataFim}`
      );

      const text = await response.text();
      let dados;
      try {
        dados = JSON.parse(text);
      } catch {
        dados = {};
      }

      if (response.ok && Array.isArray(dados.movimentacoes)) {
        setMovimentacoes(dados.movimentacoes);
        setMensagem("");
      } else if (response.ok && (!dados.movimentacoes || dados.movimentacoes.length === 0)) {
        setMovimentacoes([]);
        setMensagem("Nenhuma movimentação encontrada neste período.");
      } else {
        setMovimentacoes([]);
        setMensagem(dados.mensagem || text || "Erro ao carregar extrato.");
      }
    } catch (e) {
      setMensagem("Erro ao conectar com o servidor.");
    }
  };

  const exportarPDF = () => {
    if (!dataInicio || !dataFim) {
      alert("Preencha as datas para gerar o PDF!");
      return;
    }
    window.open(
      `http://localhost:8080/api/contas/${usuario.cpf}/extrato/pdf?inicio=${dataInicio}&fim=${dataFim}`,
      "_blank"
    );
  };

  return (
    <div className="page">
      <div
        className="card"
        style={{
          width: "500px",
          maxHeight: "90vh",
          overflowY: "auto",
          paddingBottom: "20px",
        }}
      >
        <h2>Extrato Bancário</h2>
        <p>Selecione o período:</p>

        <div className="form">
          <input
            type="date"
            value={dataInicio}
            onChange={(e) => setDataInicio(e.target.value)}
          />
          <input
            type="date"
            value={dataFim}
            onChange={(e) => setDataFim(e.target.value)}
          />
          <button className="btn cadastro" onClick={buscarExtrato}>
            Buscar Extrato
          </button>
          <button className="btn cadastro" onClick={exportarPDF}>
            Exportar em PDF
          </button>
        </div>

        {mensagem && (
          <p style={{ marginTop: "15px", color: "#555", fontWeight: "bold" }}>
            {mensagem}
          </p>
        )}

        {movimentacoes.length > 0 && (
          <div
            style={{
              marginTop: "20px",
              overflowX: "auto",
              maxHeight: "300px",
              overflowY: "auto",
              border: "1px solid #ddd",
              borderRadius: "8px",
              background: "#fff",
            }}
          >
            <table className="extrato-table" style={{ width: "100%" }}>
              <thead>
                <tr>
                  <th>Data</th>
                  <th>Tipo</th>
                  <th>Valor (R$)</th>
                </tr>
              </thead>
              <tbody>
                {movimentacoes.map((mov, index) => (
                  <tr key={index}>
                    <td>{mov.dataHora}</td>
                    <td>{mov.tipo}</td>
                    <td
                      style={{
                        color: mov.valor < 0 ? "red" : "green",
                        fontWeight: "bold",
                      }}
                    >
                      {mov.valor.toFixed(2)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <button
          className="btn login"
          style={{ marginTop: "20px" }}
          onClick={() => navigate("/menu")}
        >
          Voltar ao Menu
        </button>
      </div>
    </div>
  );
}
