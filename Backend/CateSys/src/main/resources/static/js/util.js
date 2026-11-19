// Funções que as telas de matrícula e desmatrícula usam em comum.
// Precisa ser carregado antes do matricula.js e do desmatricula.js.

const el = id => document.getElementById(id);

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

// ---------- Erros e mensagens (sem alert) ----------

function marcarErro(campo, mensagem) {
    el(campo).classList.add('is-invalid');
    el('erro-' + campo).textContent = mensagem;
}

function limparErros(campos) {
    campos.forEach(c => el(c).classList.remove('is-invalid'));
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

// ---------- Chamadas à API ----------

async function buscarJson(url) {
    const resp = await fetch(url);
    if (!resp.ok) throw new Error('Falha ao carregar ' + url);
    return resp.json();
}

// Monta as opções de um <select>. O texto entra com textContent, nunca como HTML.
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

// Envia os dados por POST. Devolve a resposta se deu certo, ou null se deu erro (a tela já mostra o aviso).
// campos: liga o nome do campo que a API devolve ao id do campo na tela
async function enviarPost(url, dados, campos, botao) {
    botao.disabled = true;
    try {
        const resp = await fetch(url, {
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
            return null;
        }
        if (!resp.ok) throw new Error('Falha ao enviar');
        return corpo;
    } catch (e) {
        mostrarMensagem('Não foi possível concluir a operação. Tente novamente.', 'danger');
        return null;
    } finally {
        botao.disabled = false;
    }
}