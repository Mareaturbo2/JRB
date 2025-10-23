import { useEffect, useState } from "react";
import {
  solicitarCartaoCredito,
  solicitarCartaoDebito,
  obterInfoCartaoCredito,
  obterInfoCartaoDebito,
  compraCredito,
  compraDebito,
  pagarFatura,
  gerarFaturaPdf, 
} from "../utils/api";
import "../App.css";

export default function Cartoes() {
  const usuario = JSON.parse(localStorage.getItem("usuario"));
  const cpf = usuario?.cpf;
  const tipoConta = (usuario?.tipo || "").toLowerCase(); // "corrente" e "poupanca"

  // estados separados evita um apagar o outro
  const [credito, setCredito] = useState(null);
  const [debito, setDebito] = useState(null);
  const [temCredito, setTemCredito] = useState(false);
  const [temDebito, setTemDebito] = useState(false);

  // inputs
  const [descricao, setDescricao] = useState("");
  const [valor, setValor] = useState("");
  const [limite, setLimite] = useState("");

  // ui
  const [mensagem, setMensagem] = useState("");
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    if (!cpf) return;
    const carregar = async () => {
      setCarregando(true);
      try {
        // crédito
        if (tipoConta !== "poupanca") {
          try {
            const c = await obterInfoCartaoCredito(cpf);
            if (c && c.numeroCartao) {
              setCredito(c);
              setTemCredito(true);
            } else {
              setCredito(null);
              setTemCredito(false);
            }
          } catch {
            setCredito(null);
            setTemCredito(false);
          }
        } else {
          setCredito(null);
          setTemCredito(false);
        }

        // débito
        try {
          const d = await obterInfoCartaoDebito(cpf);
          if (d && d.numeroCartao) {
            setDebito(d);
            setTemDebito(true);
          } else {
            setDebito(null);
            setTemDebito(false);
          }
        } catch {
          setDebito(null);
          setTemDebito(false);
        }
      } finally {
        setCarregando(false);
      }
    };
    carregar();
  }, [cpf, tipoConta]);

  const showError = (e, fallback) => {
    // pega mensagem vinda do backend 
    const msg =
      (e && e.message) ||
      (typeof e === "string" ? e : "") ||
      fallback ||
      "Erro inesperado.";
    setMensagem(msg);
  };

  //solicitar cartões
  const handleSolicitarCredito = async () => {
    if (tipoConta === "poupanca") {
      setMensagem("Contas poupança não podem solicitar cartão de crédito.");
      return;
    }
    if (!limite || Number(limite) <= 0) {
      setMensagem("Informe um limite válido.");
      return;
    }
    try {
      const r = await solicitarCartaoCredito(cpf, Number(limite));
      setMensagem(r?.mensagem || "Cartão de crédito solicitado com sucesso!");
      const c = await obterInfoCartaoCredito(cpf);
      setCredito(c || null);
      setTemCredito(!!c?.numeroCartao);
    } catch (e) {
      showError(e, "Erro ao solicitar cartão de crédito.");
    }
  };

  const handleSolicitarDebito = async () => {
    try {
      const r = await solicitarCartaoDebito(cpf);
      setMensagem(r?.mensagem || "Cartão de débito solicitado com sucesso!");
      const d = await obterInfoCartaoDebito(cpf);
      setDebito(d || null);
      setTemDebito(!!d?.numeroCartao);
    } catch (e) {
      showError(e, "Erro ao solicitar cartão de débito.");
    }
  };

  //compras
  const handleCompraCredito = async () => {
    const v = Number(valor);
    if (!v || v <= 0) {
      setMensagem("Informe um valor válido para compra.");
      return;
    }

    //evita chamar o backend se já sabemos que vai estourar
    const disponivel = Number(credito?.limiteDisponivel ?? 0);
    if (disponivel && v > disponivel) {
      setMensagem(`Valor acima do limite disponível (R$ ${disponivel.toFixed(2)}).`);
      return;
    }

    try {
      const r = await compraCredito(cpf, v, descricao || "Compra crédito");
      setMensagem(r?.mensagem || "Compra no crédito registrada com sucesso!");
      setValor("");
      setDescricao("");
      //recarrega só o crédito
      const c = await obterInfoCartaoCredito(cpf);
      setCredito(c || null);
      setTemCredito(!!c?.numeroCartao);
    } catch (e) {
      showError(e, "Erro na compra no crédito.");
    }
  };

 const handleCompraDebito = async () => {
  const v = Number(valor);
  if (!v || v <= 0) {
    setMensagem("Informe um valor válido para compra.");
    return;
  }

  try {
    const resp = await compraDebito(cpf, v, (descricao && descricao.trim()) || "Compra débito");

    if (resp?.erro) {            
      setMensagem(resp.erro);     // ex: saldo insulficiente
      return;
    }

    setMensagem(resp?.mensagem || "Compra no débito registrada com sucesso!");
    setValor("");
    setDescricao("");
    //débito não tem info de saldo nada para recarregar aqui
  } catch {
    setMensagem("Erro de conexão ao tentar comprar no débito.");
  }
};

  //fatura
  const handlePagarFatura = async () => {
    try {
      const r = await pagarFatura(cpf);
      setMensagem(r?.mensagem || "Fatura paga com sucesso!");
      const c = await obterInfoCartaoCredito(cpf);
      setCredito(c || null);
      setTemCredito(!!c?.numeroCartao);
    } catch (e) {
      showError(e, "Erro ao pagar fatura.");
    }
  };

  const handleBaixarPDF = () => {
    if (!credito?.numeroCartao) {
      setMensagem("Número do cartão de crédito não encontrado.");
      return;
    }
    try {
      gerarFaturaPdf(cpf, credito.numeroCartao);
    } catch (e) {
      showError(e, "Erro ao baixar fatura.");
    }
  };

  return (
    <div className="page">
      <div className="card" style={{ width: 520 }}>
        <h2>Cartões</h2>

        {carregando && <p>Carregando…</p>}

        {/* solicitações */}
        {(!temCredito || !temDebito) && (
          <div className="form" style={{ marginBottom: 16 }}>
            {!temCredito && tipoConta !== "poupanca" && (
              <>
                <h3>Solicitar Cartão de Crédito</h3>
                <input
                  type="number"
                  placeholder="Limite desejado (R$)"
                  value={limite}
                  onChange={(e) => setLimite(e.target.value)}
                />
                <button className="btn cadastro" onClick={handleSolicitarCredito}>
                  Solicitar Crédito
                </button>
              </>
            )}

            {!temDebito && (
              <>
                <h3>Solicitar Cartão de Débito</h3>
                <button className="btn cadastro" onClick={handleSolicitarDebito}>
                  Solicitar Débito
                </button>
              </>
            )}
          </div>
        )}

        {/* Crédito */}
        {temCredito && credito && (
          <div className="form" style={{ marginTop: 8 }}>
            <h3>Cartão de Crédito</h3>
            <p><strong>Número:</strong> {credito.numeroCartao}</p>
            <p><strong>Limite Total:</strong> R$ {(credito.limiteTotal ?? 0).toFixed(2)}</p>
            <p><strong>Disponível:</strong> R$ {(credito.limiteDisponivel ?? 0).toFixed(2)}</p>
            <p><strong>Fatura:</strong> R$ {(credito.valorFatura ?? 0).toFixed(2)}</p>

            <h4>Realizar Compra</h4>
            <input
              type="text"
              placeholder="Descrição"
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
            />
            <input
              type="number"
              placeholder="Valor"
              value={valor}
              onChange={(e) => setValor(e.target.value)}
            />
            <button className="btn cadastro" onClick={handleCompraCredito}>
              Comprar no Crédito
            </button>

            <button className="btn cadastro" onClick={handlePagarFatura}>
              Pagar Fatura
            </button>

            <button className="btn cadastro" onClick={handleBaixarPDF}>
              Baixar Fatura PDF
            </button>
          </div>
        )}

        {/* Débito */}
        {temDebito && debito && (
          <div className="form" style={{ marginTop: 24 }}>
            <h3>Cartão de Débito</h3>
            <p><strong>Número:</strong> {debito.numeroCartao}</p>
            {debito.validade && <p><strong>Validade:</strong> {debito.validade}</p>}
            {debito.cvv && <p><strong>CVV:</strong> {debito.cvv}</p>}

            <h4>Compra no Débito</h4>
            <input
              type="text"
              placeholder="Descrição"
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
            />
            <input
              type="number"
              placeholder="Valor"
              value={valor}
              onChange={(e) => setValor(e.target.value)}
            />
            <button className="btn cadastro" onClick={handleCompraDebito}>
              Comprar no Débito
            </button>
          </div>
        )}

        {mensagem && (
          <p style={{ marginTop: 15, color: "#222", fontWeight: "bold" }}>{mensagem}</p>
        )}

        <a href="/menu" className="btn login" style={{ marginTop: 15 }}>
          Voltar
        </a>
      </div>
    </div>
  );
}
