// src/utils/auth.js

export function getCpfLogado() {
  const usuario = localStorage.getItem("usuario");
  if (!usuario) return null;
  try {
    const dados = JSON.parse(usuario);
    return dados.cpf || null;
  } catch {
    return null;
  }
}
