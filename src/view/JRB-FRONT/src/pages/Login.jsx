import { useState } from "react";
import { useNavigate } from "react-router-dom";

export default function Login() {
  const [cpf, setCpf] = useState("");
  const [senha, setSenha] = useState("");
  const [mensagem, setMensagem] = useState("");
  const navigate = useNavigate();

  async function handleLogin(e) {
    e.preventDefault();

    try {
      const response = await fetch("http://localhost:8080/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ cpf, senha }),
      });

      if (!response.ok) {
        const erro = await response.json();
        setMensagem("❌ " + (erro.erro || "Falha ao fazer login."));
        return;
      }

      const dados = await response.json();

      //salva todos os dados da conta no localStorage
      localStorage.setItem("usuario", JSON.stringify({ ...dados, cpf }));


      setMensagem("✅ Login realizado com sucesso!");
      setTimeout(() => navigate("/menu"), 1000);

    } catch (err) {
      console.error("Erro ao logar:", err);
      setMensagem("❌ Erro de conexão com o servidor.");
    }
  }

  return (
    <div className="login-container">
      <h2>Login</h2>
      <form onSubmit={handleLogin}>
        <input
          type="text"
          placeholder="CPF"
          value={cpf}
          onChange={(e) => setCpf(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Senha"
          value={senha}
          onChange={(e) => setSenha(e.target.value)}
          required
        />
        <button type="submit">Entrar</button>
      </form>
      <p>{mensagem}</p>
    </div>
  );
}
