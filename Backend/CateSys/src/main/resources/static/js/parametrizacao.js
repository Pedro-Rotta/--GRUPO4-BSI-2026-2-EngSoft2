// View da parametrização. Só cuida de máscara, destaque dos campos e chamada da API.
// As regras de negócio ficam na Model (o servidor valida tudo de novo).

const API = '/api/parametrizacao';

// Os ids dos campos do formulário são os mesmos nomes do JSON da API
const CAMPOS = ['nome', 'cnpj', 'responsavel', 'telefone', 'email', 'site',
    'endereco', 'bairro', 'cidade', 'cep', 'uf', 'logotipoGrande', 'logotipoPequeno'];

// Campos que têm máscara. No banco eles vão só com números (o CNPJ, o CEP e o telefone).
const CAMPOS_COM_MASCARA = ['cnpj', 'cep', 'telefone', 'uf'];
const CAMPOS_SO_NUMEROS = ['cnpj', 'cep', 'telefone'];

const el = id => document.getElementById(id);
const valor = id => el(id).value.trim();
const soDigitos = v => (v || '').replace(/\D/g, '');

// ---------- Máscaras ----------

// No padrão, o "0" é um número; qualquer outro caractere é fixo (. / - ( ) espaço)
function aplicarMascara(valor, padrao) {
    const digitos = soDigitos(valor);
    let saida = '';
    let i = 0;
    for (const c of padrao) {
        if (i >= digitos.length) break;
        saida += (c === '0') ? digitos[i++] : c;
    }
    return saida;
}

function formatar(campo, v) {
    const texto = v || '';
    const digitos = soDigitos(texto);
    if (campo === 'cnpj') return aplicarMascara(digitos, '00.000.000/0000-00');
    if (campo === 'cep') return aplicarMascara(digitos, '00000-000');
    if (campo === 'telefone')
        return aplicarMascara(digitos, digitos.length > 10 ? '(00) 00000-0000' : '(00) 0000-0000');
    if (campo === 'uf') return texto.replace(/[^a-zA-Z]/g, '').toUpperCase().slice(0, 2);
    return texto;
}

// ---------- Validação do formulário (só conveniência) ----------

const REGRAS = {
    nome: v => v ? '' : 'Informe o nome.',
    cnpj: v => !soDigitos(v) ? 'Informe o CNPJ.'
        : soDigitos(v).length !== 14 ? 'O CNPJ deve ter 14 números.' : '',
    responsavel: v => v ? '' : 'Informe o responsável.',
    telefone: v => v && ![10, 11].includes(soDigitos(v).length)
        ? 'Telefone inválido. Informe o DDD e o número.' : '',
    email: v => v && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v) ? 'E-mail inválido.' : '',
    cep: v => v && soDigitos(v).length !== 8 ? 'CEP inválido. Informe 8 números.' : '',
    uf: v => v && v.length !== 2 ? 'Informe os 2 caracteres da UF.' : ''
};

function marcarErro(campo, mensagem) {
    el(campo).classList.add('is-invalid');
    el('erro-' + campo).textContent = mensagem;
}

function limparErros() {
    CAMPOS.forEach(c => el(c).classList.remove('is-invalid'));
}

// Retorna quantos campos estão com erro
function validarFormulario() {
    let total = 0;
    CAMPOS.forEach(c => {
        const mensagem = REGRAS[c] ? REGRAS[c](valor(c)) : '';
        if (mensagem) {
            marcarErro(c, mensagem);
            total++;
        }
    });
    return total;
}

// ---------- Mensagens na própria tela (sem alert) ----------

function mostrarMensagem(texto, tipo) {   // tipo: success, danger ou info
    const area = el('mensagem');
    area.textContent = texto;
    area.className = 'alert alert-' + tipo;
    area.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

function esconderMensagem() {
    el('mensagem').className = 'alert d-none';
}

// ---------- Modo da tela ----------

function modoCadastro() {
    el('titulo').textContent = 'Cadastrar parametrização';
    el('grupoData').classList.add('d-none');
}

function modoEdicao() {
    el('titulo').textContent = 'Alterar parametrização';
    el('grupoData').classList.remove('d-none');
}

// "2026-11-11" -> "11/11/2026"
function formatarData(iso) {
    const [ano, mes, dia] = iso.split('-');
    return dia + '/' + mes + '/' + ano;
}

// ---------- Chamadas à API ----------

async function carregar() {
    try {
        const resp = await fetch(API);
        if (resp.status === 404) {          // ainda não existe: tela de cadastro
            modoCadastro();
            return;
        }
        if (!resp.ok) throw new Error('Falha ao carregar');
        const dados = await resp.json();
        CAMPOS.forEach(c => { el(c).value = formatar(c, dados[c] ?? ''); });
        el('dataCriacao').value = dados.dataCriacao ? formatarData(dados.dataCriacao) : '';
        modoEdicao();
    } catch (e) {
        mostrarMensagem('Não foi possível carregar a parametrização. Verifique se o servidor está no ar.', 'danger');
    }
}

async function salvar(evento) {
    evento.preventDefault();
    limparErros();
    esconderMensagem();

    if (validarFormulario() > 0) {
        mostrarMensagem('Corrija os campos destacados.', 'danger');
        return;
    }

    // Manda CNPJ, CEP e telefone só com números
    const dados = {};
    CAMPOS.forEach(c => {
        dados[c] = CAMPOS_SO_NUMEROS.includes(c) ? soDigitos(valor(c)) : valor(c);
    });

    const botao = el('btnSalvar');
    botao.disabled = true;
    try {
        const resp = await fetch(API, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dados)
        });
        const corpo = await resp.json().catch(() => ({}));

        if (resp.status === 400 && corpo.erros) {     // a Model recusou: marca cada campo
            Object.entries(corpo.erros).forEach(([campo, mensagem]) => {
                if (el(campo)) marcarErro(campo, mensagem);
            });
            mostrarMensagem('Corrija os campos destacados.', 'danger');
            return;
        }
        if (!resp.ok) throw new Error('Falha ao salvar');

        mostrarMensagem(corpo.mensagem, 'success');
        if (window.atualizarCabecalho) window.atualizarCabecalho();   // o cabeçalho já mostra o nome e o logo novos
        await carregar();                              // atualiza o título e a data de cadastro
    } catch (e) {
        mostrarMensagem('Não foi possível salvar. Tente novamente.', 'danger');
    } finally {
        botao.disabled = false;
    }
}

// ---------- Início ----------

document.addEventListener('DOMContentLoaded', () => {
    CAMPOS.forEach(c => {
        el(c).addEventListener('input', () => {
            if (CAMPOS_COM_MASCARA.includes(c)) el(c).value = formatar(c, el(c).value);
            el(c).classList.remove('is-invalid');      // tirou o destaque ao voltar a digitar
        });
    });
    el('formParametrizacao').addEventListener('submit', salvar);
    carregar();
});