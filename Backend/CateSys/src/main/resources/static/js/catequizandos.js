// View: só chama a API /api/catequizandos e mostra o resultado.
// A validação daqui é conveniência; a Model valida de novo no servidor.

const API = "/api/catequizandos";
const CAMPOS = ["nome", "dataNascimento", "sexo", "documento", "telefone", "email",
    "nomeResponsavel", "telefoneResponsavel", "emailResponsavel",
    "endereco", "bairro", "cidade", "uf", "cep"];

const $ = (id) => document.getElementById(id);
const form = $("formCatequizando");

// ---------- Máscaras ----------
const mascaras = {
    telefone(v) {
        v = v.replace(/\D/g, "").slice(0, 11);
        if (v.length <= 2) return v.length ? "(" + v : v;
        if (v.length <= 6) return `(${v.slice(0, 2)}) ${v.slice(2)}`;
        if (v.length <= 10) return `(${v.slice(0, 2)}) ${v.slice(2, 6)}-${v.slice(6)}`;
        return `(${v.slice(0, 2)}) ${v.slice(2, 7)}-${v.slice(7)}`;
    },
    cep(v) {
        v = v.replace(/\D/g, "").slice(0, 8);
        return v.length > 5 ? `${v.slice(0, 5)}-${v.slice(5)}` : v;
    }
};
document.querySelectorAll("[data-mascara]").forEach((input) => {
    input.addEventListener("input", () => {
        input.value = mascaras[input.dataset.mascara](input.value);
    });
});
$("uf").addEventListener("input", (e) => e.target.value = e.target.value.toUpperCase());
$("dataNascimento").max = new Date().toISOString().slice(0, 10);

// Campos de nome não aceitam números: apaga o dígito assim que é digitado ou colado.
document.querySelectorAll("[data-nome-completo]").forEach((input) => {
    input.addEventListener("input", () => {
        if (/\d/.test(input.value)) input.value = input.value.replace(/\d/g, "");
    });
});

// ---------- Regras da tela ----------
const IDADE_SEM_RESPONSAVEL = 18;

function calcularIdade(iso) {
    if (!iso) return null;
    const [a, m, d] = iso.split("-").map(Number);
    const hoje = new Date();
    let idade = hoje.getFullYear() - a;
    if (hoje.getMonth() + 1 < m || (hoje.getMonth() + 1 === m && hoje.getDate() < d)) idade--;
    return idade;
}

// Com 18 anos ou mais, nome e telefone do responsável deixam de ser obrigatórios.
function atualizarResponsavel() {
    const idade = calcularIdade($("dataNascimento").value);
    const opcional = idade !== null && idade >= IDADE_SEM_RESPONSAVEL;
    $("nomeResponsavel").required = !opcional;
    $("telefoneResponsavel").required = !opcional;
    document.querySelectorAll(".obrigatorio-resp").forEach((s) => s.classList.toggle("oculto", opcional));
    $("avisoResponsavel").classList.toggle("oculto", !opcional);
}
$("dataNascimento").addEventListener("input", atualizarResponsavel);
$("dataNascimento").addEventListener("change", atualizarResponsavel);

// ---------- Notificação (sem alert) ----------
function notificar(tipo, mensagem, erros) {
    const area = $("notificacao");
    area.className = "notificacao " + tipo;
    area.innerHTML = "";
    const texto = document.createElement("strong");
    texto.textContent = mensagem;
    area.appendChild(texto);
    if (erros && erros.length) {
        const ul = document.createElement("ul");
        erros.forEach((e) => {
            const li = document.createElement("li");
            li.textContent = e;
            ul.appendChild(li);
        });
        area.appendChild(ul);
    }
    area.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

function limparNotificacao() {
    $("notificacao").className = "notificacao oculto";
}

// ---------- Chamada à API ----------
async function chamarApi(url, opcoes = {}) {
    let resposta;
    try {
        resposta = await fetch(url, {
            headers: { "Content-Type": "application/json" },
            ...opcoes
        });
    } catch (e) {
        $("modalErro").classList.remove("oculto"); // erro fatal de conexão
        throw e;
    }
    const corpo = await resposta.json().catch(() => null);
    return { ok: resposta.ok, status: resposta.status, corpo };
}

function mostrarErroApi(r, padrao) {
    const erros = r.corpo && r.corpo.erros ? r.corpo.erros : [];
    notificar("erro", `${padrao} (HTTP ${r.status})`, erros);
}

$("btnFecharModal").addEventListener("click", () => $("modalErro").classList.add("oculto"));

// ---------- Formulário ----------
function lerFormulario() {
    const dados = {};
    CAMPOS.forEach((c) => {
        const v = $(c).value.trim();
        dados[c] = v === "" ? null : v;
    });
    return dados;
}

function preencherFormulario(c) {
    $("id").value = c.id;
    CAMPOS.forEach((campo) => $(campo).value = c[campo] ?? "");
    atualizarResponsavel();
    $("tituloFormulario").textContent = "Alterar Catequizando";
    $("btnSalvar").textContent = "Salvar alteração";
    $("btnCancelar").textContent = "Cancelar alteração";
    form.classList.remove("validado");
    form.scrollIntoView({ behavior: "smooth" });
}

function limparFormulario() {
    form.reset();
    atualizarResponsavel();
    $("id").value = "";
    $("tituloFormulario").textContent = "Cadastrar Catequizando";
    $("btnSalvar").textContent = "Cadastrar";
    $("btnCancelar").textContent = "Limpar";
    form.classList.remove("validado");
}

form.addEventListener("submit", async (e) => {
    e.preventDefault();
    limparNotificacao();
    form.classList.add("validado");
    if (!form.checkValidity()) {
        notificar("erro", "Corrija os campos destacados.");
        return;
    }

    const id = $("id").value;
    const alterando = id !== "";
    const botao = $("btnSalvar");
    botao.disabled = true;
    try {
        const r = await chamarApi(alterando ? `${API}/${id}` : API, {
            method: alterando ? "PUT" : "POST",
            body: JSON.stringify(lerFormulario())
        });
        if (r.ok) {
            notificar("sucesso", r.corpo.mensagem);
            limparFormulario();
            buscar();
        } else {
            mostrarErroApi(r, alterando ? "Não foi possível alterar" : "Não foi possível cadastrar");
        }
    } finally {
        botao.disabled = false;
    }
});

$("btnCancelar").addEventListener("click", () => {
    limparFormulario();
    limparNotificacao();
});

// ---------- Consulta ----------
function formatarData(iso) {
    if (!iso) return "";
    const [a, m, d] = iso.split("-");
    return `${d}/${m}/${a}`;
}

function celula(texto) {
    const td = document.createElement("td");
    td.textContent = texto ?? "";
    return td;
}

function botao(texto, classe, acao) {
    const b = document.createElement("button");
    b.type = "button";
    b.className = "btn pequeno " + classe;
    b.textContent = texto;
    b.addEventListener("click", acao);
    return b;
}

function montarAcoes(td, c) {
    td.innerHTML = "";
    td.append(
        botao("Editar", "", () => carregarParaAlterar(c.id)),
        botao("Excluir", "perigo", () => pedirConfirmacao(td, c))
    );
}

// Confirmação na própria linha, sem confirm()
function pedirConfirmacao(td, c) {
    td.innerHTML = "";
    const aviso = document.createElement("span");
    aviso.className = "confirmar";
    aviso.textContent = "Excluir?";
    td.append(
        aviso,
        botao("Sim", "perigo", () => excluir(c)),
        botao("Não", "", () => montarAcoes(td, c))
    );
}

function renderizar(lista) {
    const corpo = $("tabelaCorpo");
    corpo.innerHTML = "";
    if (!lista.length) {
        const tr = document.createElement("tr");
        const td = celula("Nenhum catequizando encontrado.");
        td.colSpan = 6;
        td.className = "vazio";
        tr.appendChild(td);
        corpo.appendChild(tr);
        return;
    }
    lista.forEach((c) => {
        const tr = document.createElement("tr");
        const local = [c.cidade, c.uf].filter(Boolean).join("/");
        tr.append(
            celula(c.nome),
            celula(formatarData(c.dataNascimento)),
            celula(c.nomeResponsavel),
            celula(c.telefoneResponsavel),
            celula(local)
        );
        const acoes = document.createElement("td");
        acoes.className = "col-acoes";
        montarAcoes(acoes, c);
        tr.appendChild(acoes);
        corpo.appendChild(tr);
    });
}

async function buscar() {
    const params = new URLSearchParams();
    const nome = $("filtroNome").value.trim();
    const responsavel = $("filtroResponsavel").value.trim();
    if (nome) params.set("nome", nome);
    if (responsavel) params.set("responsavel", responsavel);

    const r = await chamarApi(`${API}?${params}`);
    if (r.ok) renderizar(r.corpo);
    else mostrarErroApi(r, "Não foi possível consultar");
}

async function carregarParaAlterar(id) {
    limparNotificacao();
    const r = await chamarApi(`${API}/${id}`);
    if (r.ok) preencherFormulario(r.corpo);
    else mostrarErroApi(r, "Não foi possível carregar o catequizando");
}

async function excluir(c) {
    limparNotificacao();
    const r = await chamarApi(`${API}/${c.id}`, { method: "DELETE" });
    if (r.ok) {
        notificar("sucesso", r.corpo.mensagem);
        if ($("id").value === String(c.id)) limparFormulario();
        buscar();
    } else {
        mostrarErroApi(r, "Não foi possível excluir");
    }
}

$("formFiltro").addEventListener("submit", (e) => {
    e.preventDefault();
    buscar();
});

buscar();
