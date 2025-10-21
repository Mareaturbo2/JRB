import { useEffect, useState } from "react";
import { getCpfLogado, extratoJson } from "../utils/api";

export default function Extrato() {
  const [extrato, setExtrato] = useState([]);
  const [erro, setErro] = useState("");
  const [inicio, setInicio] = useState("");
  const [fim, setFim] = useState("");
  const usuario = JSON.parse(localStorage.getItem("usuario")) || {};
const cpf = usuario.cpf;


  const carregarExtrato = async (i = null, f = null) => {
    try {
      let url = `/contas/${cpf}/extrato`;
      if (i && f) {
        url += `?inicio=${i}&fim=${f}`;
      }

      const resp = await fetch(`http://localhost:8080/api${url}`);
      const dados = await resp.json();

      if (!dados || !Array.isArray(dados.movimentacoes)) {
        setErro("Não há movimentações registradas.");
        setExtrato([]);
        return;
      }

      const filtrado = dados.movimentacoes.filter(
        (item) => item && item.tipo && item.valor !== undefined
      );

      setExtrato(filtrado);
      setErro("");
    } catch (err) {
      setErro("Erro ao carregar extrato: " + err.message);
    }
  };

  useEffect(() => {
    carregarExtrato();
  }, []);

  const baixarPDF = () => {
    window.open(`http://localhost:8080/api/contas/${cpf}/extrato/pdf`, "_blank");
  };

  const aplicarFiltro = (e) => {
    e.preventDefault();
    if (inicio && fim) {
      carregarExtrato(inicio, fim);
    }
  };

  return (
    <div style={{ color: "white", textAlign: "center", marginTop: "50px" }}>
      <h2>Extrato da Conta</h2>

      <form
        onSubmit={aplicarFiltro}
        style={{ marginBottom: "20px", display: "flex", justifyContent: "center", gap: "10px" }}
      >
        <div>
          <label>Início:</label>
          <input
            type="date"
            value={inicio}
            onChange={(e) => setInicio(e.target.value)}
            style={{ marginLeft: "5px" }}
          />
        </div>
        <div>
          <label>Fim:</label>
          <input
            type="date"
            value={fim}
            onChange={(e) => setFim(e.target.value)}
            style={{ marginLeft: "5px" }}
          />
        </div>
        <button type="submit">Filtrar</button>
      </form>

      {erro && <p>{erro}</p>}

      {!erro && extrato.length > 0 && (
        <table
          style={{
            margin: "20px auto",
            borderCollapse: "collapse",
            color: "white",
            width: "70%",
          }}
        >
          <thead>
            <tr style={{ borderBottom: "2px solid white" }}>
              <th>Data</th>
              <th>Tipo</th>
              <th>Valor</th>
            </tr>
          </thead>
          <tbody>
            {extrato.map((item, index) => (
              <tr key={index} style={{ borderBottom: "1px solid gray" }}>
                <td>{item.dataHora}</td>
                <td>{item.tipo}</td>
                <td style={{ color: item.valor < 0 ? "red" : "lightgreen" }}>
                  R$ {item.valor.toFixed(2)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <div style={{ marginTop: "20px" }}>
        <button onClick={baixarPDF}>📄 Exportar PDF</button>
      </div>

      <a href="/menu" style={{ color: "#6f6fff", display: "block", marginTop: "20px" }}>
        Voltar
      </a>
    </div>
  );
}
