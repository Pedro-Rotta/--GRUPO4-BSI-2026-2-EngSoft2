// Tela de matrícula: matricular (F1) e desmatricular (F2).

const API = '/api/matriculas';

const el = id => document.getElementById(id);

let matriculados = [];   // guarda a lista de matriculados para mostrar a turma atual de quem for escolhido
let turmas = [];         // guarda as turmas com vaga para mostrar as vagas livres

// ---------- Textos ----------

// "EUCARISTIA_1" vira "Eucaristia 1"
function nomeDaEtapa(etapa) {
    const texto = (etapa || '').replace(/_/g, ' ').toLowerCase();
    return texto.charAt(0).toUpperCase() + texto.slice(1);
}

function descricaoDaTurma(t) {
    return nomeDaEtapa(t.etapa) + ' · ' + t.anoOferta;
}

// "2026-11-17" vira "17/11/2026"
function formatarData(iso) {
    const [ano, mes, dia] = (iso || '').split('-');
    return dia + '/' + mes + '/' + ano;
}

// ---------- Erros e mensagens ----------

function marcarErro(campo, mensagem) {
    el(campo).classList.add('is-invalid');
    el('erro-' + campo).textContent = mensagem;
}

function limparErros() {
    ['matCatequizando', 'matTurma', 'desCatequizando', 'desMotivo']
        .forEach(c => el(c).classList.remove('is-invalid'));
}

function mostrarMensagem(texto, tipo) {   // tipo: success ou danger
    const area = el('mensagem');
    area.textContent = texto;
    area.className = 'alert alert-' + tipo;
    area.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

function esconderMensagem() {
    el('mensagem').className = 'alert d-none';
}

// ---------- Listas ----------

async function buscar(caminho) {
    const resp = await fetch(API + caminho);
    if (!resp.ok) throw new Error('Falha ao carregar ' + caminho);
    return resp.json();
}

function preencherSelect(select, itens, rotuloPadrao, valorDe, textoDe) {
    select.replaceChildren();
    const primeira = document.createElement('option');
    primeira.value = '';
    primeira.textContent = itens.length ? rotuloPadrao : 'Nenhum disponível';
    select.appendChild(primeira);
    itens.forEach(item => {
        const opcao = document.createElement('option');
        opcao.value = valorDe(item);
        opcao.textContent = textoDe(item);
        select.appendChild(opcao);
    });
}

async function carregarListas() {
    try {
        const [disponiveis, comVaga, matriculadosAgora] = await Promise.all([
            buscar('/catequizandos-disponiveis'),
            buscar('/turmas-com-vaga'),
            buscar('/catequizandos-matriculados')
        ]);
        turmas = comVaga;
        matriculados = matriculadosAgora;
        preencherSelect(el('matCatequizando'), disponiveis, 'Selecione o catequizando',
            c => c.catequizandoId, c => c.nome);
        preencherSelect(el('matTurma'), comVaga, 'Selecione a turma',
            t => t.turmaId, t => descricaoDaTurma(t) + ' (turma ' + t.turmaId + ') · ' + t.vagasLivres + ' vagas');
        preencherSelect(el('desCatequizando'), matriculadosAgora, 'Selecione o catequizando',
            c => c.catequizandoId, c => c.nome);
        atualizarResumos();
    } catch (e) {
        mostrarMensagem('Não foi possível carregar as listas. Verifique se o servidor está no ar.', 'danger');
    }
}

// Mostra, antes de confirmar, o que vai acontecer
function atualizarResumos() {
    const nomeMat = el('matCatequizando').selectedOptions[0];
    const turma = turmas.find(t => String(t.turmaId) === el('matTurma').value);
    el('resumoMatricula').textContent = (el('matCatequizando').value && turma)
        ? 'Matricular ' + nomeMat.textContent + ' na turma ' + descricaoDaTurma(turma) + ' (' + turma.vagasLivres + ' vagas livres).'
        : '';

    const atual = matriculados.find(c => String(c.catequizandoId) === el('desCatequizando').value);
    el('resumoDesmatricula').textContent = atual
        ? 'Turma atual: ' + descricaoDaTurma(atual) + ' (turma ' + atual.turmaId + '). Confirme para encerrar a matrícula e liberar a vaga.'
        : '';
}

// ---------- Envio ----------

async function enviar(caminho, dados, campos, botao) {
    botao.disabled = true;
    try {
        const resp = await fetch(API + caminho, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dados)
        });
        const corpo = await resp.json().catch(() => ({}));

        if (resp.status === 400 && corpo.erros) {          // a Model recusou: marca cada campo
            Object.entries(corpo.erros).forEach(([campo, mensagem]) => {
                if (campos[campo]) marcarErro(campos[campo], mensagem);
            });
            mostrarMensagem('Corrija os campos destacados.', 'danger');
            return;
        }
        if (!resp.ok) throw new Error('Falha ao enviar');

        const data = formatarData(corpo.dataMatricula || corpo.dataDesistencia);
        mostrarMensagem(corpo.mensagem + ' — matrícula nº ' + corpo.matriculaId
            + ', catequizando ' + corpo.catequizandoId + ', turma ' + corpo.turmaId + ', em ' + data + '.', 'success');
        el('desMotivo').value = '';
        await carregarListas();                            // as listas mudaram
    } catch (e) {
        mostrarMensagem('Não foi possível concluir a operação. Tente novamente.', 'danger');
    } finally {
        botao.disabled = false;
    }
}

function matricular(evento) {
    evento.preventDefault();
    limparErros();
    esconderMensagem();

    const catequizandoId = el('matCatequizando').value;
    const turmaId = el('matTurma').value;
    let faltou = false;
    if (!catequizandoId) { marcarErro('matCatequizando', 'Selecione o catequizando.'); faltou = true; }
    if (!turmaId) { marcarErro('matTurma', 'Selecione a turma.'); faltou = true; }
    if (faltou) {
        mostrarMensagem('Corrija os campos destacados.', 'danger');
        return;
    }
    enviar('', { catequizandoId: Number(catequizandoId), turmaId: Number(turmaId) },
        { catequizandoId: 'matCatequizando', turmaId: 'matTurma' }, el('btnMatricular'));
}

function desmatricular(evento) {
    evento.preventDefault();
    limparErros();
    esconderMensagem();

    const catequizandoId = el('desCatequizando').value;
    if (!catequizandoId) {
        marcarErro('desCatequizando', 'Selecione o catequizando.');
        mostrarMensagem('Corrija os campos destacados.', 'danger');
        return;
    }
    enviar('/desmatricular', { catequizandoId: Number(catequizandoId), motivo: el('desMotivo').value.trim() },
        { catequizandoId: 'desCatequizando', motivo: 'desMotivo' }, el('btnDesmatricular'));
}

// ---------- Início ----------

document.addEventListener('DOMContentLoaded', () => {
    ['matCatequizando', 'matTurma', 'desCatequizando', 'desMotivo'].forEach(id => {
        el(id).addEventListener('input', () => {
            el(id).classList.remove('is-invalid');     // tirou o destaque ao mexer no campo
            atualizarResumos();
        });
    });
    el('formMatricular').addEventListener('submit', matricular);
    el('formDesmatricular').addEventListener('submit', desmatricular);
    carregarListas();
});