
const RAIZ = window.location.pathname.includes("/pages/") ? "../" : "";

function sair() {
  localStorage.removeItem("token");
  localStorage.removeItem("nomeUsuario");
  window.location.href = RAIZ + "index.html";
}

function tokenValido() {
  const token = localStorage.getItem("token");
  if (!token) return false;
  try {
    const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
    return payload.exp * 1000 > Date.now();   // o backend dá 2h de validade
  } catch (e) {
    return false;
  }
}

if (!tokenValido()) sair();

async function apiFetch(caminho, opcoes = {}) {
  const resp = await fetch(API_URL + caminho, {
    ...opcoes,
    headers: {
      "Content-Type": "application/json",
      ...(opcoes.headers || {}),
      "Authorization": "Bearer " + localStorage.getItem("token")
    }
  });
  if (resp.status === 401 || resp.status === 403) sair();
  return resp;
}

document.addEventListener("DOMContentLoaded", () => {
  //botão sair
  document.querySelectorAll(".btn-logout").forEach(a =>
    a.addEventListener("click", (e) => { e.preventDefault(); sair(); }));

  const nome = localStorage.getItem("nomeUsuario");
  if (nome) {
    const iniciais = nome.trim().split(/\s+/).map(p => p[0]).slice(0, 2).join("").toUpperCase();
    const strong = document.querySelector(".sidebar-user strong");
    if (strong) strong.textContent = nome;
    document.querySelectorAll(".avatar, .top-avatar").forEach(el => el.textContent = iniciais);
  }
});