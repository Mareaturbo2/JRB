import { useNavigate } from "react-router-dom";
import "../App.css";

export default function Menu() {
  const navigate = useNavigate();
  const usuario = JSON.parse(localStorage.getItem("usuario")) || {};

  
  const tipoNormalizado = String(usuario?.tipo || "")
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "")
    .toLowerCase()
    .trim();
  const isPoupanca = tipoNormalizado.includes("poupanca");

  return (
    <div className="page">
      <div className="card" style={{ width: 320 }}>
        <h2>Bem-vindo, {usuario.titular || "Cliente"}</h2>
        <p>Conta {isPoupanca ? "Poupança" : "Corrente"}{" "}</p>
        <p>{usuario?.conta ? `( ${usuario.conta} )` : `( ${usuario.cpf} )`}</p>
        <p style={{ marginBottom: 16 }}>Selecione uma operação</p>

        <div className="form" style={{ gap: 10 }}>
          <button className="btn cadastro" onClick={() => navigate("/saldo")}>Saldo</button>
          <button className="btn cadastro" onClick={() => navigate("/deposito")}>Depósito</button>
          <button className="btn cadastro" onClick={() => navigate("/saque")}>Saque</button>
          <button className="btn cadastro" onClick={() => navigate("/transferencia")}>Transferência</button>
          <button className="btn cadastro" onClick={() => navigate("/pagamento")}>Pagamento</button>
          <button className="btn cadastro" onClick={() => navigate("/cartoes")}>Cartões</button>
          <button className="btn cadastro" onClick={() => navigate("/extrato")}>Extrato</button>

          {/* só aparece para poupança */}
          {isPoupanca && (
            <>
              <button className="btn cadastro" onClick={() => navigate("/investir-poupanca")}>
                Investir
              </button>
              <button className="btn cadastro" onClick={() => navigate("/resgatar-poupanca")}>
                Resgatar
              </button>
            </>
          )}

          <button className="btn cadastro" onClick={() => navigate("/encerrar")}>
            Encerrar Conta
          </button>

          <button
            className="btn login"
            onClick={() => {
              localStorage.removeItem("usuario");
              navigate("/login");
            }}
          >
            Sair
          </button>
        </div>
      </div>
    </div>
  );
}
