const sidebar = document.getElementById("sidebar");
const overlay = document.getElementById("sidebarOverlay");
const toggle  = document.getElementById("menuToggle");

function setMenu(open) {
  sidebar.classList.toggle("open", open);
  overlay.classList.toggle("show", open);
  toggle.setAttribute("aria-expanded", open);
}

toggle.addEventListener("click", () => setMenu(!sidebar.classList.contains("open")));

overlay.addEventListener("click", () => setMenu(false));
document.addEventListener("keydown", e => { if (e.key === "Escape") setMenu(false); });

window.matchMedia("(min-width: 992px)").addEventListener("change", e => {
  if (e.matches) setMenu(false);
});