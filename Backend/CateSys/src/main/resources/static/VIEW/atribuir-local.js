// View do RF_F8 - Atribuir Local à Turma: só chama a API /api/turmas e mostra o resultado.

const API = "/api/turmas";
const $ = (id) => document.getElementById(id);
let turmaAtual = null;
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
const textoHorario = (h) => h ? `${DIAS[h.diaSemana] ?? h.diaSemana ?? "?"} ${h.horaInicio}–${h.horaFim}` : "—";
const textoData = (iso) => iso ? iso.split("-").reverse().join("/") : "?";
const textoPeriodo = (t) => `${textoData(t.dataInicio)} a ${textoData(t.dataFim)}`;

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

function celula(texto) {
    const td = document.createElement("td");
    td.textContent = texto ?? "";
    return td;
}

// ---------- Lista de turmas ----------
async function listarTurmas() {
    const r = await chamarApi(API);
    const corpo = $("tabelaTurmas");
    corpo.innerHTML = "";
    if (!r.ok || !r.corpo.length) {
        const tr = document.createElement("tr");
        const td = celula(r.ok ? "Nenhuma turma cadastrada." : "Não foi possível carregar as turmas.");
        td.colSpan = 8;
        td.className = "vazio";
        tr.appendChild(td);
        corpo.appendChild(tr);
        return;
    }
    r.corpo.forEach((t) => {
        const tr = document.createElement("tr");
        tr.append(
            celula(t.id),
            celula(`${ETAPAS[t.etapa] ?? t.etapa} (${t.anoOferta})`),
            celula(textoPeriodo(t)),
            celula(t.catequista ?? "—"),
            celula(t.local ?? "—"),
            celula(textoHorario(t.horario)),
            celula(t.status)
        );
        const acoes = document.createElement("td");
        acoes.className = "col-acoes";
        const botao = document.createElement("button");
        botao.type = "button";
        botao.className = "btn pequeno";
        botao.textContent = "Selecionar";
        botao.addEventListener("click", () => {
            $("turmaId").value = t.id;
            buscarTurma(t.id);
        });
        acoes.appendChild(botao);
        tr.appendChild(acoes);
        corpo.appendChild(tr);
    });
}

// ---------- Passos 1 e 2: busca a turma ----------
function mostrarDadosTurma(t) {
    $("tituloTurma").textContent = `Turma ${t.id} — ${ETAPAS[t.etapa] ?? t.etapa}`;
    const linhas = [
        ["Id da turma", t.id],
        ["Catequista", t.catequista ? `${t.catequista} (id ${t.catequistaId})` : "—"],
        ["Período", textoPeriodo(t)],
        ["Local atual", t.local ? `${t.local} (id ${t.localId})` : "Sem local"],
        ["Horário atual", textoHorario(t.horario)],
        ["Vagas", `${t.vagasDisponiveis} disponíveis de ${t.vagasTotais}`],
        ["Status", t.status]
    ];
    const corpo = $("dadosTurma");
    corpo.innerHTML = "";
    linhas.forEach(([rotulo, valor]) => {
        const tr = document.createElement("tr");
        const th = document.createElement("th");
        th.textContent = rotulo;
        tr.append(th, celula(valor));
        corpo.appendChild(tr);
    });
}

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

function resetarHorarios(texto) {
    preencherSelect($("horario"), [], null, null, texto);
    $("horario").disabled = true;
}

async function buscarTurma(id) {
    limparNotificacao();
    const r = await chamarApi(`${API}/${id}`);
    if (!r.ok) {
        $("painelLocal").classList.add("oculto");
        notificar("erro", r.status === 404 ? "Turma não encontrada" : `Erro ao buscar a turma (HTTP ${r.status})`,
            r.corpo?.erros);
        return;
    }
    turmaAtual = r.corpo.turma;
    locais = r.corpo.locais;
    mostrarDadosTurma(turmaAtual);
    $("formLocal").classList.remove("validado");
    $("avisoLocal").classList.add("oculto");
    preencherSelect($("local"), locais, (l) => l.id, (l) => `${l.nome} (capacidade ${l.capacidade})`,
        "Não há locais disponíveis");
    resetarHorarios("Escolha o local primeiro");
    if (!locais.length)
        notificar("erro", "Não há locais disponíveis. Cadastre um local antes de atribuir.");
    $("painelLocal").classList.remove("oculto");
    $("painelLocal").scrollIntoView({ behavior: "smooth" });
}

$("formBusca").addEventListener("submit", (e) => {
    e.preventDefault();
    const id = $("turmaId").value;
    if (!id || Number(id) < 1) {
        notificar("erro", "Informe um id de turma válido.");
        return;
    }
    buscarTurma(id);
});

// ---------- Passo 4: horários disponíveis do local ----------
$("local").addEventListener("change", async () => {
    const localId = $("local").value;
    const aviso = $("avisoLocal");
    aviso.classList.add("oculto");
    if (!localId) {
        resetarHorarios("Escolha o local primeiro");
        return;
    }
    const local = locais.find((l) => String(l.id) === localId);
    if (local && turmaAtual.vagasTotais > local.capacidade) {
        aviso.textContent = `Atenção: "${local.nome}" comporta ${local.capacidade} pessoas e a turma tem `
            + `${turmaAtual.vagasTotais} vagas.`;
        aviso.classList.remove("oculto");
    }
    resetarHorarios("Carregando...");
    const r = await chamarApi(`${API}/${turmaAtual.id}/locais/${localId}/horarios`);
    if (!r.ok) {
        resetarHorarios("Erro ao carregar");
        notificar("erro", `Não foi possível carregar os horários (HTTP ${r.status})`, r.corpo?.erros);
        return;
    }
    preencherSelect($("horario"), r.corpo, (h) => h.id, textoHorario,
        "Nenhum horário livre neste local no período da turma");
    $("horario").disabled = r.corpo.length === 0;
});

// ---------- Passos 5 a 7: atribui e mostra a turma completa ----------
$("formLocal").addEventListener("submit", async (e) => {
    e.preventDefault();
    limparNotificacao();
    const form = $("formLocal");
    form.classList.add("validado");
    // select desabilitado não entra no checkValidity(), por isso confere o horário à parte
    if (!form.checkValidity() || !$("horario").value) {
        notificar("erro", "Selecione o local e um horário disponível.");
        return;
    }
    const botao = $("btnAtribuir");
    botao.disabled = true;
    try {
        const r = await chamarApi(`${API}/${turmaAtual.id}/local`, {
            method: "PUT",
            body: JSON.stringify({ localId: Number($("local").value), horarioId: Number($("horario").value) })
        });
        if (r.ok) {
            turmaAtual = r.corpo.turma;
            mostrarDadosTurma(turmaAtual);
            notificar("sucesso", r.corpo.mensagem + ` — Turma ${turmaAtual.id}: ${turmaAtual.local}, `
                + textoHorario(turmaAtual.horario));
            form.reset();
            form.classList.remove("validado");
            resetarHorarios("Escolha o local primeiro");
            listarTurmas();
        } else {
            const titulo = r.status === 409 ? "Conflito de agenda" : "Não foi possível atribuir o local";
            notificar("erro", `${titulo} (HTTP ${r.status})`, r.corpo?.erros);
        }
    } finally {
        botao.disabled = false;
    }
});

$("btnFechar").addEventListener("click", () => {
    $("painelLocal").classList.add("oculto");
    turmaAtual = null;
});

listarTurmas();
