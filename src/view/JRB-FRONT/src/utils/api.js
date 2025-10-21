// src/api.js
const API_URL = "http://localhost:8080/api";

export async function criarConta(dados) {
  const resp = await fetch(`${API_URL}/contas`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dados),
  });
  return resp.json();
}

export async function consultarSaldo(cpf) {
  const resp = await fetch(`${API_URL}/contas/${cpf}/saldo`);
  return resp.json();
}

export async function depositar(cpf, valor) {
  const resp = await fetch(`${API_URL}/contas/${cpf}/deposito`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ valor }),
  });
  return resp.json();
}

export async function sacar(cpf, valor) {
  const resp = await fetch(`${API_URL}/saque`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ cpf, valor }),
  });

  const text = await resp.text();
  if (!resp.ok) throw new Error(text);
  return text;
}


export async function transferir(cpfOrigem, cpfDestino, valor) {
  const resp = await fetch(`${API_URL}/contas/transferir`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ cpfOrigem, cpfDestino, valor }),
  });

  if (!resp.ok) throw new Error(await resp.text());
  return resp.json();
}


export async function pagarBoleto(cpf, codigo, valor, dataVencimento = null) {
  const resp = await fetch(`${API_URL}/contas/${cpf}/pagamento`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ codigo, valor, dataVencimento }),
  });
  return resp.json();
}


// 🔹 Solicitar cartão de crédito
export async function solicitarCartaoCredito(cpf, limite) {
  const resp = await fetch(`${API_URL}/contas/${cpf}/cartoes/credito`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ limite }),
  });
  return resp.json();
}

// 🔹 Solicitar cartão de débito
export async function solicitarCartaoDebito(cpf) {
  const resp = await fetch(`${API_URL}/contas/${cpf}/cartoes/debito`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
  });
  return resp.json();
}

export async function investirPoupanca(cpf, valor) {
  const resp = await fetch(`${API_URL}/contas/${cpf}/poupanca/investir`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ valor }),
  });

  const data = await resp.json();
  if (!resp.ok) throw new Error(data.erro || "Erro ao investir na poupança.");
  return data.mensagem || "Investimento realizado com sucesso.";
}

export async function resgatarPoupanca(cpf, valor) {
  const resp = await fetch(`${API_URL}/contas/${cpf}/poupanca/resgatar`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ valor }),
  });

  const data = await resp.json();
  if (!resp.ok) throw new Error(data.erro || "Erro ao resgatar da poupança.");
  return data.mensagem || "Resgate realizado com sucesso.";
}


export async function gerarExtratoPdf(cpf) {
  window.open(`${API_URL}/contas/${cpf}/extrato/pdf`, "_blank");
}

export async function gerarFaturaPdf(cpf, numeroCartao) {
  window.open(`${API_URL}/contas/${cpf}/cartoes/${numeroCartao}/fatura/pdf`, "_blank");
}
export async function login(cpf, senha) {
  const resp = await fetch(`${API_URL}/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ cpf, senha }),
  });

  // O back retorna texto puro, então pegamos como texto
  const text = await resp.text();

  if (!resp.ok) {
    throw new Error(text || "Erro ao fazer login");
  }

  return text;
}
export async function extratoJson(cpf) {
  const resp = await fetch(`${API_URL}/contas/${cpf}/extrato`);
  if (!resp.ok) {
    throw new Error("Erro ao buscar extrato");
  }
  return await resp.json();
}


// 💸 Pagar conta ou boleto
export async function pagar(cpf, valor, descricao) {
  const resp = await fetch(`${API_URL}/contas/${cpf}/pagamento`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ valor, descricao }),
  });

  if (!resp.ok) {
    throw new Error("Erro ao pagar conta ou boleto");
  }

  return await resp.json();
}
// 💳 Compra com Cartão de Crédito
export async function compraCredito(cpf, valor, descricao) {
  const resp = await fetch(`${API_URL}/contas/${cpf}/cartoes/credito/compra`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ valor, descricao }),
  });
  return resp.json();
}
// 💳 Compra com Cartão de Débito
export async function compraDebito(cpf, valor, descricao) {
  const resp = await fetch(`${API_URL}/contas/${cpf}/cartoes/debito/compra`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ valor, descricao }),
  });
  return resp.json();
}
// 💳 Pagar fatura do cartão de crédito
export async function pagarFatura(cpf) {
  const resp = await fetch(`${API_URL}/contas/${cpf}/cartoes/pagar-fatura`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
  });
  return resp.json();
}
// 🏦 Encerrar conta
export async function encerrarConta(cpf) {
  const resp = await fetch(`${API_URL}/contas/${cpf}/encerrar`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
  });

  const msg = await resp.text();
  if (!resp.ok) throw new Error(msg || "Erro ao encerrar a conta.");

  return msg;
}
// ==========================================================
// 🔹 Função utilitária para pegar o CPF do usuário logado
// ==========================================================
export function getCpfLogado() {
  try {
    const dados = JSON.parse(localStorage.getItem("usuario"));
    return dados?.cpf || null;
  } catch {
    return null;
  }
}


// 💳 Obter informações do cartão de crédito
export async function obterInfoCartaoCredito(cpf) {
  const resp = await fetch(`${API_URL}/contas/${cpf}/cartoes/credito/info`);
  if (!resp.ok) {
    const msg = await resp.text();
    throw new Error(msg || "Erro ao buscar informações do cartão.");
  }
  return await resp.json();
}

// 💳 Baixar fatura PDF automaticamente
export async function baixarFaturaPDF(cpf, numeroCartao) {
  window.open(`${API_URL}/contas/${cpf}/cartoes/${numeroCartao}/fatura/pdf`, "_blank");
}

// 🔍 Obter todos os detalhes da conta (tipo, saldo, poupança etc.)
export async function detalhesConta(cpf) {
  const resp = await fetch(`${API_URL}/contas/${cpf}`);
  if (!resp.ok) {
    const msg = await resp.text();
    throw new Error(msg || "Erro ao buscar detalhes da conta.");
  }
  return await resp.json();
}
export async function obterInfoCartaoDebito(cpf) {
  const resp = await fetch(`${API_URL}/contas/${cpf}/cartoes/debito/info`);
  if (!resp.ok) throw new Error("Erro ao buscar cartão de débito");
  return await resp.json();
}
