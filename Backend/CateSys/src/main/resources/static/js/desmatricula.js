// Tela de desmatricular catequizando (F2).
// Só cuida da tela e das chamadas à API. As regras (matrícula ativa, motivo...) ficam na Model, no servidor.

const API = '/api/desmatriculas';
const CAMPOS = ['desCatequizando', 'desMotivo'];

let matriculados = [];   // guarda a lista para mostrar a turma atual de quem for escolhido

async function carregarListas() {
    try {
        matriculados = await buscarJson(API + '/catequizandos-matriculados');
        preencherSelect(el('desCatequizando'), matriculados, 'Selecione o catequizando',
            c => c.catequizandoId, c => c.nome);
        atualizarResumo();
    } catch (e) {
        mostrarMensagem('Não foi possível carregar a lista. Verifique se o servidor está no ar.', 'danger');
    }
}

// Mostra, antes de confirmar, a turma em que o catequizando está
function atualizarResumo() {
    const atual = matriculados.find(c => String(c.catequizandoId) === el('desCatequizando').value);
    el('resumoDesmatricula').textContent = atual
        ? 'Turma atual: ' + descricaoDaTurma(atual) + ' (turma ' + atual.turmaId + '). Confirme para encerrar a matrícula e liberar a vaga.'
        : '';
}

async function desmatricular(evento) {
    evento.preventDefault();
    limparErros(CAMPOS);
    esconderMensagem();

    const catequizandoId = el('desCatequizando').value;
    if (!catequizandoId) {
        marcarErro('desCatequizando', 'Selecione o catequizando.');
        mostrarMensagem('Corrija os campos destacados.', 'danger');
        return;
    }

    const corpo = await enviarPost(API, { catequizandoId: Number(catequizandoId), motivo: el('desMotivo').value.trim() },
        { catequizandoId: 'desCatequizando', motivo: 'desMotivo' }, el('btnDesmatricular'));
    if (corpo) {
        mostrarMensagem(corpo.mensagem + ' — matrícula nº ' + corpo.matriculaId + ', catequizando '
            + corpo.catequizandoId + ', turma ' + corpo.turmaId + ', em ' + formatarData(corpo.dataDesistencia) + '.', 'success');
        el('desMotivo').value = '';
        await carregarListas();                            // a lista mudou
    }
}

document.addEventListener('DOMContentLoaded', () => {
    CAMPOS.forEach(id => {
        el(id).addEventListener('input', () => {
            el(id).classList.remove('is-invalid');         // tirou o destaque ao mexer no campo
            atualizarResumo();
        });
    });
    el('formDesmatricular').addEventListener('submit', desmatricular);
    carregarListas();
});