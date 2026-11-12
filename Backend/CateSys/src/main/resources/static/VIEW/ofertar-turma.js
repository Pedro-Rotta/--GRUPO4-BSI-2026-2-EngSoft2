// View do RF_F5 - Ofertar Turma: só chama a API /api/turmas e mostra o resultado.

const API = "/api/turmas";
const $ = (id) => document.getElementById(id);
const form = $("formTurma");
let locais = [];

const ETAPAS = {
    EUCARISTIA_1: "1ª Eucaristia — 1ª etapa",
    EUCARISTIA_2: "1ª Eucaristia — 2ª etapa",
    CRISMA_1: "Crisma — 1ª etapa",
    CRISMA_2: "Crisma — 2ª etapa"
};
const DIAS = {
    DOMINGO: "Domingo", SEGUNDA: "Segunda", TERCA: "Terça", QUARTA: "Quarta",
    QUINTA: "Quinta", SEXTA: "Sexta", SABADO: "Sábado"
};
const textoHorario = (h) => h ? `${DIAS[h.diaSemana] ?? h.diaSemana ?? "?"} ${h.horaInicio}–${h.horaFim}` : "";
const textoData = (iso) => iso ? iso.split("-").reverse().join("/") : "";

// ---------- Notificação e API ----------
function notificar(tipo, mensagem, erros) {
    const area = $("notificacao");
    area.className = "notificacao " + tipo;
    area.innerHTML = "";
    const forte = document.createElement("strong");
    forte.textContent = mensagem;
    area.appendChild(forte);
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

const limparNotificacao = () => $("notificacao").className = "notificacao oculto";

async function chamarApi(url, opcoes = {}) {
    let resposta;
    try {
        resposta = await fetch(url, { headers: { "Content-Type": "application/json" }, ...opcoes });
    } catch (e) {
        $("modalErro").classList.remove("oculto");
        throw e;
    }
    const corpo = await resposta.json().catch(() => null);
    return { ok: resposta.ok, status: resposta.status, corpo };
}

$("btnFecharModal").addEventListener("click", () => $("modalErro").classList.add("oculto"));

// ---------- Passo 2: carrega as opções ----------
function preencherSelect(select, itens, valor, texto, vazio) {
    select.innerHTML = "";
    const primeira = document.createElement("option");
    primeira.value = "";
    primeira.textContent = itens.length ? "Selecione..." : vazio;
    select.appendChild(primeira);
    itens.forEach((item) => {
        const opcao = document.createElement("option");
        opcao.value = valor(item);
        opcao.textContent = texto(item);
        select.appendChild(opcao);
    });
}

async function carregarOpcoes() {
    const r = await chamarApi(`${API}/opcoes`);
    if (!r.ok) {
        notificar("erro", `Não foi possível carregar as opções (HTTP ${r.status})`, r.corpo?.erros);
        return;
    }
    const o = r.corpo;
    locais = o.locais;
    preencherSelect($("etapa"), o.etapas, (e) => e, (e) => ETAPAS[e] ?? e, "Nenhuma etapa");
    preencherSelect($("catequista"), o.catequistas, (c) => c.id, (c) => c.descricao, "Nenhum catequista cadastrado");
    preencherSelect($("local"), o.locais, (l) => l.id, (l) => `${l.nome} (capacidade ${l.capacidade})`,
        "Nenhum local cadastrado");
}

// ---------- Horários livres: consulta o banco assim que local e período estiverem preenchidos ----------
let consultaHorarios = 0; // ignora respostas de consultas antigas

function bloquearHorarios(texto, aviso) {
    preencherSelect($("horario"), [], null, null, texto);
    $("horario").disabled = true;
    $("avisoHorario").textContent = aviso ?? "";
    $("avisoHorario").classList.toggle("oculto", !aviso);
}

async function atualizarHorarios() {
    const localId = $("local").value;
    const inicio = $("dataInicio").value;
    const fim = $("dataFim").value;
    if (!localId || !dataCompleta(inicio) || !dataCompleta(fim)) {
        bloquearHorarios("Escolha o local e o período primeiro");
        return;
    }
    if (fim <= inicio) {
        bloquearHorarios("Período inválido", "A data de término deve ser posterior à data de início.");
        return;
    }
    const anterior = $("horario").value;
    const numero = ++consultaHorarios;
    bloquearHorarios("Buscando horários...");
    const params = new URLSearchParams({ localId, dataInicio: inicio, dataFim: fim });
    if ($("catequista").value) params.set("catequistaId", $("catequista").value);

    const r = await chamarApi(`${API}/horarios-disponiveis?${params}`);
    if (numero !== consultaHorarios) return;
    if (!r.ok) {
        bloquearHorarios("Erro ao buscar horários");
        notificar("erro", `Não foi possível buscar os horários (HTTP ${r.status})`, r.corpo?.erros);
        return;
    }
    if (!r.corpo.length) {
        const quem = $("catequista").value ? "para este local e catequista" : "neste local";
        bloquearHorarios("Não há horários disponíveis", `Não há horários disponíveis ${quem} no período informado.`
            + " Troque o local, o período ou o catequista.");
        return;
    }
    preencherSelect($("horario"), r.corpo, (h) => h.id, textoHorario, "");
    $("horario").disabled = false;
    if (r.corpo.some((h) => String(h.id) === anterior)) $("horario").value = anterior;
    $("avisoHorario").textContent = `${r.corpo.length} horário(s) disponível(is) no período.`;
    $("avisoHorario").classList.remove("oculto");
}

// ---------- Capacidade do local (fluxo alternativo: vagas > capacidade) ----------
function localSelecionado() {
    return locais.find((l) => String(l.id) === $("local").value);
}

function conferirCapacidade() {
    const local = localSelecionado();
    const vagas = $("vagas");
    const aviso = $("avisoCapacidade");
    vagas.max = local ? local.capacidade : "";
    vagas.setCustomValidity("");
    aviso.classList.add("oculto");
    if (!local) return;

    aviso.classList.remove("oculto");
    aviso.innerHTML = "";
    if (Number(vagas.value) > local.capacidade) {
        vagas.setCustomValidity("acima da capacidade");
        $("erroVagas").textContent = `Máximo de ${local.capacidade} vagas neste local.`;
        aviso.append(`O local "${local.nome}" comporta ${local.capacidade} pessoas. `);
        const botao = document.createElement("button");
        botao.type = "button";
        botao.className = "btn pequeno";
        botao.textContent = `Limitar a ${local.capacidade} vagas`;
        botao.addEventListener("click", () => {
            vagas.value = local.capacidade;
            conferirCapacidade();
        });
        aviso.appendChild(botao);
    } else {
        $("erroVagas").textContent = "Informe um número de vagas válido.";
        aviso.textContent = `Capacidade do local: ${local.capacidade} pessoas.`;
    }
}

// Não mexe no atributo "min" do campo de data: alterá-lo enquanto o usuário digita faz o Chrome
// redesenhar o campo e apagar o que estava sendo digitado. A regra fica só na validação.
function conferirDatas() {
    const inicio = $("dataInicio").value;
    const fim = $("dataFim");
    fim.setCustomValidity(inicio && fim.value && fim.value <= inicio ? "término antes do início" : "");
}

// Enquanto o ano é digitado o campo já tem valor ("0002", "0020"...): só considera ano com 4 dígitos.
const dataCompleta = (valor) => /^\d{4}-\d{2}-\d{2}$/.test(valor) && Number(valor.slice(0, 4)) >= 1900;

// Espera a pessoa parar de digitar antes de consultar o banco.
let esperaHorarios;
function agendarHorarios() {
    clearTimeout(esperaHorarios);
    esperaHorarios = setTimeout(atualizarHorarios, 400);
}

$("local").addEventListener("change", () => { conferirCapacidade(); atualizarHorarios(); });
$("vagas").addEventListener("input", conferirCapacidade);
$("dataInicio").addEventListener("change", () => { conferirDatas(); agendarHorarios(); });
$("dataFim").addEventListener("change", () => { conferirDatas(); agendarHorarios(); });
$("catequista").addEventListener("change", atualizarHorarios);

// ---------- Passos 3 a 6: envia e mostra o resumo ----------
function mostrarResumo(t) {
    const linhas = [
        ["Id da turma", t.id],
        ["Etapa", ETAPAS[t.etapa] ?? t.etapa],
        ["Catequista", t.catequista],
        ["Período", `${textoData(t.dataInicio)} a ${textoData(t.dataFim)}`],
        ["Local", t.local],
        ["Horário", textoHorario(t.horario)],
        ["Vagas disponíveis", `${t.vagasDisponiveis} de ${t.vagasTotais}`],
        ["Status", t.status]
    ];
    const corpo = $("resumoCorpo");
    corpo.innerHTML = "";
    linhas.forEach(([rotulo, valor]) => {
        const tr = document.createElement("tr");
        const th = document.createElement("th");
        th.textContent = rotulo;
        const td = document.createElement("td");
        td.textContent = valor ?? "";
        tr.append(th, td);
        corpo.appendChild(tr);
    });
    $("resumo").classList.remove("oculto");
    $("resumo").scrollIntoView({ behavior: "smooth" });
}

form.addEventListener("submit", async (e) => {
    e.preventDefault();
    limparNotificacao();
    conferirDatas();
    conferirCapacidade();
    form.classList.add("validado");
    if (!form.checkValidity()) {
        notificar("erro", "Corrija os campos destacados.");
        return;
    }
    // select desabilitado não entra no checkValidity()
    if (!$("horario").value) {
        notificar("erro", $("horario").options[0]?.textContent === "Não há horários disponíveis"
            ? "Não há horários disponíveis para este local e período." : "Selecione um horário disponível.");
        return;
    }
    const dados = {
        etapa: $("etapa").value,
        catequistaId: Number($("catequista").value),
        dataInicio: $("dataInicio").value,
        dataFim: $("dataFim").value,
        localId: Number($("local").value),
        horarioId: Number($("horario").value),
        vagas: Number($("vagas").value)
    };
    const botao = $("btnOfertar");
    botao.disabled = true;
    try {
        const r = await chamarApi(API, { method: "POST", body: JSON.stringify(dados) });
        if (r.ok) {
            notificar("sucesso", r.corpo.mensagem);
            mostrarResumo(r.corpo.turma);
            limparFormulario();
        } else {
            const titulo = r.status === 409 ? "Conflito de agenda" : "Não foi possível ofertar a turma";
            notificar("erro", `${titulo} (HTTP ${r.status})`, r.corpo?.erros);
        }
    } finally {
        botao.disabled = false;
    }
});

function limparFormulario() {
    form.reset();
    form.classList.remove("validado");
    conferirCapacidade();
    bloquearHorarios("Escolha o local e o período primeiro");
}

$("btnLimpar").addEventListener("click", () => {
    limparFormulario();
    limparNotificacao();
    $("resumo").classList.add("oculto");
});

carregarOpcoes();
