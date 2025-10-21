import { useState } from "react";
import { criarConta } from "../utils/api";

export default function Cadastro() {
  const [form, setForm] = useState({ cpf: "", titular: "", senha: "1234", tipo: "corrente", saldoInicial: 0 });
  const [msg, setMsg] = useState("");
  const [erro, setErro] = useState("");

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    setMsg(""); setErro("");
    try {
      // tipo: "corrente" ou "poupanca"
      const payload = { ...form, saldoInicial: Number(form.saldoInicial) || 0 };
      const res = await criarConta(payload);
      setMsg(res?.mensagem || "Conta criada com sucesso!");
    } catch (e) {
      setErro(e.message);
    }
  };

  return (
    <div className="card">
      <h2>Abrir Conta</h2>
      <form onSubmit={onSubmit} className="form">
        <input name="cpf" placeholder="CPF" value={form.cpf} onChange={onChange} required />
        <input name="titular" placeholder="Titular" value={form.titular} onChange={onChange} required />
        <input name="senha" placeholder="Senha" value={form.senha} onChange={onChange} />
        <select name="tipo" value={form.tipo} onChange={onChange}>
          <option value="corrente">Corrente</option>
          <option value="poupanca">Poupança</option>
        </select>
        <input name="saldoInicial" type="number" step="0.01" placeholder="Saldo Inicial" value={form.saldoInicial} onChange={onChange} />
        <button type="submit">Criar Conta</button>
      </form>
      {msg && <p className="ok">{msg}</p>}
      {erro && <p className="err">{erro}</p>}
    </div>
  );
}