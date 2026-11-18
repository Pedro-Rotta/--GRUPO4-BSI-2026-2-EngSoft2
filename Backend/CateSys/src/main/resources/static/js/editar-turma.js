// View do RF_B5 - Editar Turma: lista as turmas (id, etapa, ano, status) e, no lápis,
// carrega todos os dados da turma para alterar. Só chama a API /api/turmas.

const API = "/api/turmas";
const $ = (id) => document.getElementById(id);
const form = $("formEdicao");

let turmaAtual = null;   // dados completos da turma em edição
let opcoes = null;       // etapas, catequistas, locais e horários (GET /api/turmas/opcoes)

const ETAPAS = {
    EUCARISTIA_1: "1ª Eucaristia — 1ª etapa",
    EUCARISTIA_2: "1ª Eucaristia — 2ª etapa",
    CRISMA_1: "Crisma — 1ª etapa",
    CRISMA_2: "Crisma — 2ª etapa"
};
const STATUS = {
    OFERTADA: "Ofertada",
    ABERTA: "Aberta",
    EM_ANDAMENTO: "Em andamento",
    ENCERRADA: "Encerrada",
    CANCELADA: "Cancelada"
};
const DIAS = {
    DOMINGO: "Domingo", SEGUNDA: "Segunda", TERCA: "Terça", QUARTA: "Quarta",
    QUINTA: "Quinta", SEXTA: "Sexta", SABADO: "Sábado"
};
const textoHorario = (h) => h ? `${DIAS[h.diaSemana] ?? h.diaSemana ?? "?"} ${h.horaInicio}–${h.horaFim}` : "";

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

async function chamarApi(url, opcoesFetch = {}) {
    let resposta;
    try {
        resposta = await fetch(url, { headers: { "Content-Type": "application/json" }, ...opcoesFetch });
    } catch (e) {
        $("modalErro").classList.remove("oculto");   // erro fatal de conexão
        throw e;
    }
    const corpo = await resposta.json().catch(() => null);
    return { ok: resposta.ok, status: resposta.status, corpo };
}

$("btnFecharModal").addEventListener("click", () => $("modalErro").classList.add("oculto"));

function hoje() {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

// ---------- Lista de turmas ----------
function celula(texto) {
    const td = document.createElement("td");
    td.textContent = texto ?? "-";
    return td;
}

async function listarTurmas() {
    const corpo = $("tabelaTurmas");
    const r = await chamarApi(API);
    corpo.innerHTML = "";
    if (!r.ok || !Array.isArray(r.corpo)) {
        corpo.innerHTML = '<tr><td colspan="5" class="vazio">Não foi possível carregar as turmas.</td></tr>';
        notificar("erro", `Erro ao listar turmas (HTTP ${r.status})`, r.corpo?.erros);
        return;
    }
    if (r.corpo.length === 0) {
        corpo.innerHTML = '<tr><td colspan="5" class="vazio">Nenhuma turma cadastrada.</td></tr>';
        return;
    }
    r.corpo.forEach((t) => {
        const tr = document.createElement("tr");
        tr.append(celula(t.id), celula(ETAPAS[t.etapa] ?? t.etapa), celula(t.anoOferta),
            celula(STATUS[t.status] ?? t.status));

        const acoes = document.createElement("td");
        acoes.className = "col-acoes";
        const botao = document.createElement("button");
        botao.type = "button";
        botao.className = "btn btn-sm btn-outline-primary";
        botao.innerHTML = '<i class="bi bi-pencil"></i> ';
        botao.append("Editar");
        botao.title = `Editar a turma ${t.id}`;
        botao.addEventListener("click", () => editarTurma(t.id));
        acoes.appendChild(botao);
        tr.appendChild(acoes);
        corpo.appendChild(tr);
    });
}

// ---------- Selects ----------
function preencherSelect(select, itens, valorDe, textoDe, selecionado, vazio) {
    select.innerHTML = "";
    if (vazio) {
        const primeira = document.createElement("option");
        primeira.value = "";
        primeira.textContent = vazio;
        select.appendChild(primeira);
    }
    itens.forEach((item) => {
        const opcao = document.createElement("option");
        opcao.value = valorDe(item);
        opcao.textContent = textoDe(item);
        select.appendChild(opcao);
    });
    select.value = selecionado ?? "";
}

async function carregarOpcoes() {
    if (opcoes) return true;
    const r = await chamarApi(`${API}/opcoes`);
    if (!r.ok) {
        notificar("erro", `Erro ao carregar as opções (HTTP ${r.status})`, r.corpo?.erros);
        return false;
    }
    opcoes = r.corpo;
    return true;
}

// ---------- Editar: busca TODOS os dados da turma ----------
async function editarTurma(id) {
    limparNotificacao();
    const [r] = await Promise.all([chamarApi(`${API}/${id}`), carregarOpcoes()]);
    if (!r.ok) {
        notificar("erro", r.status === 404 ? "Turma não encontrada" : `Erro ao buscar a turma (HTTP ${r.status})`,
            r.corpo?.erros);
        return;
    }
    if (!opcoes) return;
    turmaAtual = r.corpo;
    preencherFormulario(turmaAtual);
    $("painelEdicao").classList.remove("oculto");
    $("painelEdicao").scrollIntoView({ behavior: "smooth", block: "start" });
}

function preencherFormulario(t) {
    form.classList.remove("validado");
    $("tituloEdicao").textContent = `Editar turma ${t.id} — ${ETAPAS[t.etapa] ?? t.etapa}`;
    $("resumoTurma").textContent =
        `Ano de oferta ${t.anoOferta ?? "-"} · Catequista: ${t.catequista ?? "-"} · Local: ${t.local ?? "-"} · ` +
        `Horário: ${textoHorario(t.horario) || "-"} · Vagas: ${t.vagasTotais} (${t.vagasOcupadas} ocupadas, ` +
        `${t.vagasDisponiveis} livres)`;

    preencherSelect($("etapa"), opcoes.etapas, (e) => e, (e) => ETAPAS[e] ?? e, t.etapa);
    preencherSelect($("status"), t.statusPossiveis, (s) => s, (s) => STATUS[s] ?? s, t.status);

    // O catequista atual entra na lista mesmo que já não esteja ativo.
    const catequistas = [...opcoes.catequistas];
    if (t.catequistaId && !catequistas.some((c) => c.id === t.catequistaId))
        catequistas.unshift({ id: t.catequistaId, descricao: `${t.catequista} (inativo)` });
    preencherSelect($("catequista"), catequistas, (c) => c.id, (c) => c.descricao, t.catequistaId, "Selecione...");

    preencherSelect($("local"), opcoes.locais, (l) => l.id, (l) => `${l.nome} (capacidade ${l.capacidade})`,
        t.localId, "Selecione...");

    $("dataInicio").value = t.dataInicio ?? "";
    $("dataFim").value = t.dataFim ?? "";
    $("vagas").value = t.vagasTotais;
    $("vagas").min = Math.max(1, t.vagasOcupadas);

    conferirDatas();
    conferirVagas();
    atualizarHorarios(t.horarioId);
}

// ---------- Regras de conveniência na tela (a Control valida de novo) ----------
function conferirDatas() {
    const inicio = $("dataInicio").value;
    const fim = $("dataFim");
    // Só uma data de início NOVA precisa ser hoje ou depois; a turma já iniciada mantém a sua.
    const mudouInicio = turmaAtual && inicio !== turmaAtual.dataInicio;
    $("dataInicio").setCustomValidity(mudouInicio && inicio && inicio < hoje() ? "início antes de hoje" : "");
    fim.setCustomValidity(inicio && fim.value && fim.value <= inicio ? "término antes do início" : "");
}

function conferirVagas() {
    const vagas = $("vagas");
    const local = (opcoes?.locais || []).find((l) => String(l.id) === $("local").value);
    const total = Number(vagas.value);
    const ocupadas = turmaAtual ? turmaAtual.vagasOcupadas : 0;
    let mensagem = "";
    if (total && total < ocupadas)
        mensagem = `A turma já tem ${ocupadas} matrícula(s): o limite não pode ser menor.`;
    else if (local && total > local.capacidade)
        mensagem = `O local comporta ${local.capacidade} pessoas.`;
    vagas.setCustomValidity(mensagem);
    $("erroVagas").textContent = mensagem || "Informe um número de vagas válido.";
    $("avisoVagas").textContent = turmaAtual
        ? `Matrículas na turma: ${ocupadas}.` + (local ? ` Capacidade do local: ${local.capacidade}.` : "")
        : "";
}

// Horários livres no local/período para o catequista, sem contar a própria turma.
async function atualizarHorarios(manter) {
    const select = $("horario");
    const aviso = $("avisoHorario");
    const valorAnterior = manter ?? select.value;
    const localId = $("local").value;
    const inicio = $("dataInicio").value;
    const fim = $("dataFim").value;
    aviso.classList.add("oculto");

    if (!turmaAtual || !localId || !inicio || !fim || fim <= inicio) {
        preencherSelect(select, [], null, null, "", "Escolha o local e o período primeiro");
        select.disabled = true;
        return;
    }
    const params = new URLSearchParams({ localId, dataInicio: inicio, dataFim: fim });
    if ($("catequista").value) params.set("catequistaId", $("catequista").value);
    const r = await chamarApi(`${API}/${turmaAtual.id}/horarios-disponiveis?${params}`);
    if (!r.ok) {
        preencherSelect(select, [], null, null, "", "Não foi possível consultar os horários");
        select.disabled = true;
        notificar("erro", "Erro ao consultar horários", r.corpo?.erros);
        return;
    }
    const horarios = r.corpo;
    preencherSelect(select, horarios, (h) => h.id, textoHorario, "",
        horarios.length ? "Selecione..." : "Nenhum horário livre");
    select.disabled = horarios.length === 0;
    if (horarios.some((h) => String(h.id) === String(valorAnterior)))
        select.value = String(valorAnterior);
    else if (valorAnterior && String(valorAnterior) === String(turmaAtual.horarioId)) {
        aviso.textContent = "O horário atual da turma não está livre com o local, o período ou o catequista escolhidos. Escolha outro.";
        aviso.classList.remove("oculto");
    }
    if (!horarios.length) {
        aviso.textContent = "O local está ocupado (ou o catequista tem outra turma) em todos os horários desse período.";
        aviso.classList.remove("oculto");
    }
}

let esperaHorarios;
function agendarHorarios() {
    clearTimeout(esperaHorarios);
    esperaHorarios = setTimeout(() => atualizarHorarios(), 400);
}

$("local").addEventListener("change", () => { conferirVagas(); atualizarHorarios(); });
$("catequista").addEventListener("change", () => atualizarHorarios());
$("dataInicio").addEventListener("change", () => { conferirDatas(); agendarHorarios(); });
$("dataFim").addEventListener("change", () => { conferirDatas(); agendarHorarios(); });
$("vagas").addEventListener("input", conferirVagas);

// ---------- Salvar ----------
form.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    limparNotificacao();
    conferirDatas();
    conferirVagas();
    form.classList.add("validado");
    if (!form.checkValidity()) {
        notificar("erro", "Corrija os campos destacados.");
        return;
    }
    const dados = {
        etapa: $("etapa").value,
        status: $("status").value,
        catequistaId: Number($("catequista").value),
        dataInicio: $("dataInicio").value,
        dataFim: $("dataFim").value,
        localId: Number($("local").value),
        horarioId: Number($("horario").value),
        vagas: Number($("vagas").value)
    };
    const botao = $("btnSalvar");
    botao.disabled = true;
    try {
        const r = await chamarApi(`${API}/${turmaAtual.id}`, { method: "PUT", body: JSON.stringify(dados) });
        if (r.ok) {
            notificar("sucesso", r.corpo?.mensagem ?? "Turma alterada com sucesso");
            turmaAtual = r.corpo.turma;
            preencherFormulario(turmaAtual);
            listarTurmas();
        } else {
            notificar("erro", `Não foi possível alterar a turma (HTTP ${r.status})`, r.corpo?.erros);
        }
    } finally {
        botao.disabled = false;
    }
});

$("btnCancelar").addEventListener("click", () => {
    $("painelEdicao").classList.add("oculto");
    form.classList.remove("validado");
    turmaAtual = null;
    limparNotificacao();
});

listarTurmas();
