import { useEffect, useState } from "react";
import { getCpfLogado, consultarSaldo } from "../utils/api";

export default function Saldo() {
  const [dados, setDados] = useState(null);
  const [erro, setErro] = useState("");
  const usuario = JSON.parse(localStorage.getItem("usuario")) || {};
  const cpf = usuario.cpf;

  useEffect(() => {
    if (!cpf) {
      setErro("Usuário não logado.");
      return;
    }

    consultarSaldo(cpf)
      .then((dados) => {
        
        setDados({
          saldo: dados.saldo ?? 0,
          saldoDisponivel: dados.saldoDisponivel ?? 0,
          limiteChequeEspecial: dados.limiteChequeEspecial ?? null,
          investimento: dados.investimento ?? null,
        });
      })
      .catch((err) => setErro("Erro ao buscar saldo: " + err.message));
  }, [cpf]);

  if (erro) {
    return (
      <div style={{ color: "white", textAlign: "center", marginTop: "100px" }}>
        <p>{erro}</p>
        <a href="/menu" style={{ color: "#6f6fff" }}>Voltar</a>
      </div>
    );
  }

  if (!dados) {
    return (
      <div style={{ color: "white", textAlign: "center", marginTop: "100px" }}>
        <p>Carregando saldo...</p>
      </div>
    );
  }

  return (
    <div style={{ color: "white", textAlign: "center", marginTop: "80px" }}>
      <h2>Saldo da Conta</h2>

      <div
        style={{
          display: "inline-block",
          textAlign: "left",
          backgroundColor: "#222",
          padding: "20px",
          borderRadius: "10px",
          boxShadow: "0 0 10px rgba(0,0,0,0.5)",
        }}
      >
        <p>
          <strong>Saldo em Conta:</strong>{" "}
          <span style={{ color: dados.saldo >= 0 ? "lightgreen" : "red" }}>
            R$ {dados.saldo.toFixed(2)}
          </span>
        </p>

        <p>
          <strong>Saldo Disponível:</strong>{" "}
          R$ {dados.saldoDisponivel.toFixed(2)}
        </p>

        {/* se tiver cheque especial (conta corrente) */}
        {dados.limiteChequeEspecial !== null && (
          <p>
            <strong>Limite do Cheque Especial:</strong>{" "}
            R$ {dados.limiteChequeEspecial.toFixed(2)}
          </p>
        )}

        {/* se for poupança e tiver investimento */}
        {dados.investimento !== null && (
          <p>
            <strong>Investimento em Poupança:</strong>{" "}
            R$ {dados.investimento.toFixed(2)}
          </p>
        )}
      </div>

      <br />
      <a href="/menu" style={{ color: "#6f6fff" }}>Voltar</a>
    </div>
  );
}
