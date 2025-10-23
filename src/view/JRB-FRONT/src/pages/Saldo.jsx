import { useEffect, useState } from "react";
import { consultarSaldo } from "../utils/api";
import { useNavigate } from "react-router-dom";
import "../App.css";

export default function Saldo() {
  const [dados, setDados] = useState(null);
  const [erro, setErro] = useState("");
  const usuario = JSON.parse(localStorage.getItem("usuario")) || {};
  const cpf = usuario.cpf;
  const navigate = useNavigate();

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
          investimento:
            dados.poupanca?.investimento ??
            dados.investimento ??
            null,
        });
      })
      .catch((err) => setErro("Erro ao buscar saldo: " + err.message));
  }, [cpf]);

  if (erro) {
    return (
      <div className="page">
        <div className="card" style={{ width: 340 }}>
          <h2>Erro</h2>
          <p>{erro}</p>
          <button className="btn login" onClick={() => navigate("/menu")}>
            Voltar
          </button>
        </div>
      </div>
    );
  }

  if (!dados) {
    return (
      <div className="page">
        <div className="card" style={{ width: 340 }}>
          <p>Carregando saldo...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="card" style={{ width: 360 }}>
        <h2>Saldo da Conta</h2>

        <div className="form" style={{ textAlign: "left", gap: 8 }}>
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

          {dados.limiteChequeEspecial !== null && (
            <p>
              <strong>Limite do Cheque Especial:</strong>{" "}
              R$ {dados.limiteChequeEspecial.toFixed(2)}
            </p>
          )}

          {dados.investimento !== null && (
            <p>
              <strong>Investimento em Poupança:</strong>{" "}
              R$ {dados.investimento.toFixed(2)}
            </p>
          )}
        </div>

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
