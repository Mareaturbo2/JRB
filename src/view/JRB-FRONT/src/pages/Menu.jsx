import { useNavigate } from "react-router-dom";

export default function Menu() {
  //não redirecione aqui o PrivateRoute já protege.
  const usuario = JSON.parse(localStorage.getItem("usuario")) || {};
  const navigate = useNavigate();

  return (
    <div
      style={{
        color: "white",
        textAlign: "center",
        marginTop: "60px",
        fontFamily: "Arial, sans-serif",
      }}
    >
      <h1>Bem-vindo, {usuario.titular}</h1>
      <h3>
        Conta {usuario.tipo === "poupanca" ? "Poupança" : "Corrente"} ({usuario.cpf})
      </h3>
      <h3 style={{ marginBottom: "30px" }}>Selecione uma operação</h3>

      <div style={{ display: "flex", flexWrap: "wrap", gap: "10px", justifyContent: "center" }}>
        <button onClick={() => navigate("/saldo")}>💰 Ver Saldo</button>
        <button onClick={() => navigate("/deposito")}>📥 Depósito</button>
        <button onClick={() => navigate("/saque")}>🏧 Saque</button>
        <button onClick={() => navigate("/transferencia")}>🔁 Transferência</button>
        <button onClick={() => navigate("/pagamento")}>💸 Pagamento</button>
        <button onClick={() => navigate("/cartoes")}>💳 Cartões</button>
        <button onClick={() => navigate("/extrato")}>📜 Extrato</button>
        <button onClick={() => navigate("/encerrar")}>❌ Encerrar Conta</button>
       {/*botões só para conta Poupança */}
{usuario?.tipo === "poupanca" && (
  <div style={{ marginTop: "25px", background: "#f7faff", padding: "15px", borderRadius: "10px" }}>
    <h2>💎 Funções da Poupança</h2>

    <button
      onClick={() => navigate("/investir-poupanca")}
      style={{ marginRight: "10px" }}
    >
      💰 Investir na Poupança
    </button>

    <button onClick={() => navigate("/resgatar-poupanca")}>
      🔁 Resgatar da Poupança
    </button>
  </div>
)}

      </div>

      <div style={{ marginTop: 24 }}>
        <button
          onClick={() => {
            localStorage.removeItem("usuario");
            navigate("/login");
          }}
        >
          🚪 Sair
        </button>
      </div>
    </div>
  );
}
