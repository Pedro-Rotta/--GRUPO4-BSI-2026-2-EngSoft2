const sidebar = document.getElementById("sidebar");
const overlay = document.getElementById("sidebarOverlay");
const toggle  = document.getElementById("menuToggle");

function setMenu(open) {
  sidebar.classList.toggle("open", open);
  overlay.classList.toggle("show", open);
  toggle.setAttribute("aria-expanded", open);
}

// abre/fecha ao clicar no botão ☰
toggle.addEventListener("click", () => setMenu(!sidebar.classList.contains("open")));

// fecha ao clicar fora do menu ou ao apertar Esc
overlay.addEventListener("click", () => setMenu(false));
document.addEventListener("keydown", e => { if (e.key === "Escape") setMenu(false); });

// se a tela voltar a ser larga (desktop), garante que o menu não fique "aberto"
window.matchMedia("(min-width: 992px)").addEventListener("change", e => {
  if (e.matches) setMenu(false);
});