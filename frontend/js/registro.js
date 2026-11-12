const API_URL = "http://localhost:8080";

const form    = document.getElementById("registroForm");
const alerta  = document.getElementById("registroAlerta");
const botao   = document.getElementById("registroBtn");
const cpfInput = document.getElementById("regCpf");

function mostrarAlerta(msg, tipo) {
  alerta.textContent = msg;
  alerta.className = "alert notice alert-" + tipo;
}

form.addEventListener("submit", async (e) => {
  e.preventDefault();

  const nome    = form.nome.value.trim();
  const cpf     = form.cpf.value.replace(/\D/g, "");
  const email   = form.email.value.trim();
  const senha   = form.senha.value;
  const confirma = form.confirma.value;

  if (!nome || !cpf || !email || !senha) return mostrarAlerta("Preencha todos os campos.", "warning");
  if (cpf.length !== 11)                 return mostrarAlerta("CPF deve ter 11 dígitos.", "warning");
  if (!form.email.checkValidity())       return mostrarAlerta("Informe um e-mail válido.", "warning");
  if (senha.length < 6)                  return mostrarAlerta("A senha deve ter pelo menos 6 caracteres.", "warning");
  if (senha !== confirma)                return mostrarAlerta("As senhas não conferem.", "warning");

  botao.disabled = true;
  botao.textContent = "Cadastrando...";

  try {
    const resp = await fetch(API_URL + "/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ nome, cpf, email, senha })
    });

    if (resp.ok) {
      const dados = await resp.json();           // { name, token }
      localStorage.setItem("token", dados.token);
      localStorage.setItem("nomeUsuario", dados.name);
      mostrarAlerta("Cadastro realizado! Redirecionando...", "success");
      setTimeout(() => (window.location.href = "dashboard.html"), 800);
    } else if (resp.status === 400) {
      mostrarAlerta("Não foi possível cadastrar: e-mail já cadastrado ou dados inválidos.", "danger");
    } else {
      mostrarAlerta("Erro no servidor (" + resp.status + "). Tente novamente.", "danger");
    }
  } catch (err) {
    mostrarAlerta("Não foi possível conectar ao servidor.", "danger");
  } finally {
    botao.disabled = false;
    botao.textContent = "Cadastrar";
  }
});