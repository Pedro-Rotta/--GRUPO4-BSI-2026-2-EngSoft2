const form = document.getElementById("loginForm");
const alerta = document.getElementById("loginAlerta");
const botao = document.getElementById("loginBtn");

function mostrarAlerta(msg, tipo) {
  alerta.textContent = msg;
  alerta.className = "alert notice alert-" + tipo;
}

(function () {
  const token = localStorage.getItem("token");
  if (!token) return;
  try {
    const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
    if (payload.exp * 1000 > Date.now()) window.location.href = "dashboard.html";
    else localStorage.removeItem("token");
  } catch (e) {
    localStorage.removeItem("token");
  }
})();

function validaEmail(email) {
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailPattern.test(email);
}

form.addEventListener("submit", async (e) => {
  e.preventDefault();

  const email = form.email.value.trim();
  const senha = form.senha.value;

  if (!email || !senha)
    return mostrarAlerta("Informe o e-mail e a senha.", "warning");
  else {
    if (!validaEmail(email))
      return mostrarAlerta("Informe um email válido.", "warning");
    else {
      botao.disabled = true;
      botao.textContent = "Entrando...";

      try {
        const resp = await fetch(API_URL + "/auth/login", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email, senha })
        });

        if (resp.ok) {
          const dados = await resp.json();          // { name, token }
          localStorage.setItem("token", dados.token);
          localStorage.setItem("nomeUsuario", dados.name);
          window.location.href = "dashboard.html";
        } else if (resp.status === 401 || resp.status === 400) {
          mostrarAlerta("E-mail ou senha incorretos.", "danger");
        } else {
          mostrarAlerta("Erro no servidor (" + resp.status + "). Tente novamente.", "danger");
        }
      } catch (err) {
        mostrarAlerta("Não foi possível conectar ao servidor.", "danger");
      } finally {
        botao.disabled = false;
        botao.textContent = "Entrar";
      }
    }
  }
});