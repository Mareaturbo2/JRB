import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../App.css";
import { login, detalhesConta } from "../utils/api"; 

export default function Login() {
  const [cpf, setCpf] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState("");
  const navigate = useNavigate();

  async function handleLogin(e) {
    e.preventDefault();
    setErro("");

    try {
      const resposta = await login(cpf, senha);

      //se o backend retornar string tenta converter se não der usa como nome
      let dados;
      if (typeof resposta === "string") {
        try {
          dados = JSON.parse(resposta);
        } catch {
          dados = { cpf, titular: resposta, tipo: "corrente" }; // 
        }
      } else {
        dados = resposta;
      }

      // busca detalhes da conta para obter o numero
      let numeroConta = null;
      let tipoConta = dados?.tipo || "corrente";
      let titular = dados?.titular || "Cliente";

      try {
        const info = await detalhesConta(cpf);               
        numeroConta = info?.numero ?? info?.conta ?? null;   // numero da conta
        tipoConta = info?.tipo ?? tipoConta;                 // confirma tipo
        if (!dados?.titular && info?.titular) titular = info.titular; 
      } catch {
        //não travar o login se essa consulta falhar
      }

      //salva no localStorage
      const usuario = {
        cpf: dados?.cpf || cpf,
        titular,
        tipo: tipoConta,
        conta: numeroConta, 
      };

      localStorage.setItem("usuario", JSON.stringify(usuario));
      navigate("/menu");
    } catch (error) {
      console.error(error);
      setErro("CPF ou senha incorretos ou erro no servidor.");
    }
  }

  return (
    <div className="page">
      <div className="card" style={{ width: 320 }}>
        <h2>Login</h2>

        <form className="form" onSubmit={handleLogin}>
          <input
            type="text"
            placeholder="CPF"
            value={cpf}
            onChange={(e) => setCpf(e.target.value)}
            className="input"
          />
          <input
            type="password"
            placeholder="Senha"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            className="input"
          />

          {erro && <p style={{ color: "red" }}>{erro}</p>}

          <button className="btn login" type="submit">
            Entrar
          </button>
        </form>

        <button className="btn cadastro" onClick={() => navigate("/cadastro")}>
          Criar Conta
        </button>
      </div>
    </div>
  );
}
