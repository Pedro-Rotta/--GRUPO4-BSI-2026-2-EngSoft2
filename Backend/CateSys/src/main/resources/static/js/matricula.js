// Tela de matricular catequizando (F1).
// Só cuida da tela e das chamadas à API. As regras (vaga, matrícula ativa...) ficam na Model, no servidor.

const API = '/api/matriculas';
const CAMPOS = ['matCatequizando', 'matTurma'];

let turmas = [];   // guarda as turmas com vaga para mostrar as vagas livres no resumo

async function carregarListas() {
    try {
        const [disponiveis, comVaga] = await Promise.all([
            buscarJson(API + '/catequizandos-disponiveis'),
            buscarJson(API + '/turmas-com-vaga')
        ]);
        turmas = comVaga;
        preencherSelect(el('matCatequizando'), disponiveis, 'Selecione o catequizando',
            c => c.catequizandoId, c => c.nome);
        preencherSelect(el('matTurma'), comVaga, 'Selecione a turma',
            t => t.turmaId, t => descricaoDaTurma(t) + ' (turma ' + t.turmaId + ') · ' + t.vagasLivres + ' vagas');
        atualizarResumo();
    } catch (e) {
        mostrarMensagem('Não foi possível carregar as listas. Verifique se o servidor está no ar.', 'danger');
    }
}

// Mostra, antes de confirmar, o que vai acontecer
function atualizarResumo() {
    const catequizando = el('matCatequizando').selectedOptions[0];
    const turma = turmas.find(t => String(t.turmaId) === el('matTurma').value);
    el('resumoMatricula').textContent = (el('matCatequizando').value && turma)
        ? 'Matricular ' + catequizando.textContent + ' na turma ' + descricaoDaTurma(turma) + ' (' + turma.vagasLivres + ' vagas livres).'
        : '';
}

async function matricular(evento) {
    evento.preventDefault();
    limparErros(CAMPOS);
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

    const corpo = await enviarPost(API, { catequizandoId: Number(catequizandoId), turmaId: Number(turmaId) },
        { catequizandoId: 'matCatequizando', turmaId: 'matTurma' }, el('btnMatricular'));
    if (corpo) {
        mostrarMensagem(corpo.mensagem + ' — matrícula nº ' + corpo.matriculaId + ', catequizando '
            + corpo.catequizandoId + ', turma ' + corpo.turmaId + ', em ' + formatarData(corpo.dataMatricula) + '.', 'success');
        await carregarListas();                            // as listas mudaram
    }
}

document.addEventListener('DOMContentLoaded', () => {
    CAMPOS.forEach(id => {
        el(id).addEventListener('input', () => {
            el(id).classList.remove('is-invalid');         // tirou o destaque ao mexer no campo
            atualizarResumo();
        });
    });
    el('formMatricular').addEventListener('submit', matricular);
    carregarListas();
});