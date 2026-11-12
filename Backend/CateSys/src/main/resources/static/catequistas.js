
const API_URL = "/api/catequistas";
const pagina = document.body.dataset.pagina;

// FUNCOES AUXILIARES

function elemento(id) {
    return document.getElementById(id);
}

function somenteNumeros(valor) {
    return String(valor || "").replace(/\D/g, "");
}

function mascaraCpf(valor) {
    const n = somenteNumeros(valor).slice(0, 11);

    return n
        .replace(/^(\d{3})(\d)/, "$1.$2")
        .replace(/^(\d{3})\.(\d{3})(\d)/, "$1.$2.$3")
        .replace(/\.(\d{3})(\d)/, ".$1-$2");
}

function mascaraTelefone(valor) {
    const n = somenteNumeros(valor).slice(0, 11);

    if (!n) return "";

    if (n.length <= 2) {
        return `(${n}`;
    }

    const ddd = n.slice(0, 2);
    const resto = n.slice(2);

    if (resto.length <= 4) {
        return `(${ddd}) ${resto}`;
    }

    if (n.length <= 10) {
        return `(${ddd}) ${resto.slice(0, -4)}-${resto.slice(-4)}`;
    }

    return `(${ddd}) ${resto.slice(0, 5)}-${resto.slice(5)}`;
}

function cpfValido(valor) {
    const cpf = somenteNumeros(valor);

    if (!/^\d{11}$/.test(cpf)) {
        return false;
    }

    if (/^(\d)\1{10}$/.test(cpf)) {
        return false;
    }

    for (let tamanho = 9; tamanho <= 10; tamanho++) {
        let soma = 0;

        for (let i = 0; i < tamanho; i++) {
            soma += Number(cpf[i]) * (tamanho + 1 - i);
        }

        let resto = (soma * 10) % 11;

        if (resto === 10) {
            resto = 0;
        }

        if (resto !== Number(cpf[tamanho])) {
            return false;
        }
    }

    return true;
}

function formatarData(data) {
    if (!data) return "-";

    const partes = data.split("-");

    if (partes.length !== 3) {
        return data;
    }

    return `${partes[2]}/${partes[1]}/${partes[0]}`;
}

function mostrarMensagem(texto, tipo = "erro") {
    const area = elemento("mensagem");

    if (!area) return;

    area.textContent = texto;
    area.className = tipo;
}

function limparMensagem() {
    const area = elemento("mensagem");

    if (!area) return;

    area.textContent = "";
    area.className = "";
}

// REQUISICOES HTTP

async function requisicao(url, opcoes = {}) {
    let resposta;

    try {
        resposta = await fetch(url, opcoes);
    } catch (erro) {
        throw new Error(
            "Não foi possível conectar ao servidor."
        );
    }

    if (!resposta.ok) {
        const texto = await resposta.text();
        let detalhe = texto;

        try {
            const json = JSON.parse(texto);

            detalhe =
                json.message ||
                json.detail ||
                json.error ||
                texto;

        } catch (erro) {
            // O servidor pode devolver texto simples.
        }

        const erro = new Error(
            detalhe || `Erro HTTP ${resposta.status}`
        );

        erro.status = resposta.status;

        throw erro;
    }

    if (resposta.status === 204) {
        return null;
    }

    const texto = await resposta.text();

    return texto ? JSON.parse(texto) : null;
}

// VALIDAÇÃO VISUAL

function limparErroCampo(campo) {
    if (!campo) return;

    campo.classList.remove("campo-invalido");
    campo.removeAttribute("aria-invalid");

    const container = campo.closest(".campo");

    if (!container) return;

    const erro = container.querySelector(".erro-campo");

    if (erro) {
        erro.remove();
    }
}

function limparTodosErros() {
    document.querySelectorAll(".campo-invalido")
        .forEach(limparErroCampo);
}

function marcarErro(campo, texto) {
    if (!campo) return;

    limparErroCampo(campo);

    campo.classList.add("campo-invalido");
    campo.setAttribute("aria-invalid", "true");

    const container = campo.closest(".campo");

    if (container) {
        const aviso = document.createElement("span");

        aviso.className = "erro-campo";
        aviso.textContent = texto;

        container.appendChild(aviso);
    }
}

function identificarCampo(texto) {
    const t = texto.toLowerCase();

    if (t.includes("cpf")) {
        return elemento("cpf");
    }

    if (t.includes("nome")) {
        return elemento("nome");
    }

    if (t.includes("telefone")) {
        return elemento("telefone");
    }

    if (t.includes("email") || t.includes("e-mail")) {
        return elemento("email");
    }

    if (t.includes("secretaria")) {
        return elemento("secretariaId");
    }

    if (t.includes("data inicio") ||
        t.includes("data início")) {
        return elemento("dataInicio");
    }

    if (t.includes("data") ||
        t.includes("término") ||
        t.includes("termino")) {
        return elemento("dataFim");
    }

    return null;
}

function mostrarErroFormulario(erro) {
    const texto = erro.message || "Erro ao salvar.";

    mostrarMensagem(texto);

    const campo = identificarCampo(texto);

    if (campo) {
        marcarErro(campo, texto);
    }
}

// VALIDAÇÃO COMPLETA DO FORMULÁRIO

function validarFormulario() {
    const formulario = elemento("formCatequista");

    formulario.classList.add("tentou-enviar");

    limparTodosErros();

    let valido = true;
    let primeiroInvalido = null;

    function adicionarErro(id, mensagem) {
        const campo = elemento(id);

        marcarErro(campo, mensagem);

        if (!primeiroInvalido) {
            primeiroInvalido = campo;
        }

        valido = false;
    }

    const nome = elemento("nome").value.trim();
    const cpf = elemento("cpf").value;
    const telefone = somenteNumeros(
        elemento("telefone").value
    );
    const email = elemento("email").value.trim();
    const dataInicio = elemento("dataInicio").value;
    const dataFim = elemento("dataFim").value;
    const secretaria = elemento("secretariaId");

    // NOME

    if (!nome) {
        adicionarErro(
            "nome",
            "Nome obrigatório."
        );
    }

    // CPF

    if (!somenteNumeros(cpf)) {
        adicionarErro(
            "cpf",
            "CPF obrigatório."
        );

    } else if (!cpfValido(cpf)) {
        adicionarErro(
            "cpf",
            "Informe um CPF válido."
        );
    }

    // TELEFONE OPCIONAL

    if (telefone.length > 0 &&
        telefone.length !== 10 &&
        telefone.length !== 11) {

        adicionarErro(
            "telefone",
            "Telefone deve ter 10 ou 11 números."
        );
    }

    // EMAIL OPCIONAL

    if (email && !elemento("email").checkValidity()) {
        adicionarErro(
            "email",
            "Informe um e-mail válido."
        );
    }

    // DATAS OPCIONAIS

    if (dataInicio && !elemento("dataInicio").checkValidity()) {
        adicionarErro(
            "dataInicio",
            "Data de início inválida."
        );
    }

    if (dataFim && !elemento("dataFim").checkValidity()) {
        adicionarErro(
            "dataFim",
            "Data de término inválida."
        );
    }

    if (dataInicio && dataFim && dataFim < dataInicio) {
        adicionarErro(
            "dataFim",
            "Data fim não pode ser anterior à data início."
        );
    }

    // SECRETARIA OPCIONAL

    if (!secretaria.disabled &&
        !secretaria.checkValidity()) {

        adicionarErro(
            "secretariaId",
            "Informe um ID de secretaria válido."
        );
    }

    // VERIFICAR OUTRAS REGRAS NATIVAS DO HTML

    formulario.querySelectorAll(
        "input:not([type='hidden']):not(:disabled)"
    ).forEach(campo => {

        if (!campo.checkValidity() &&
            !campo.classList.contains("campo-invalido")) {

            adicionarErro(
                campo.id,
                "Preencha corretamente este campo."
            );
        }
    });

    if (!valido) {
        mostrarMensagem(
            "Existem campos inválidos. Corrija os campos destacados."
        );

        if (primeiroInvalido) {
            primeiroInvalido.focus();
        }

        return false;
    }

    limparMensagem();

    return true;
}

// DADOS DE CADASTRO

function obterDados() {
    return {
        nome: elemento("nome").value.trim(),

        cpf: somenteNumeros(
            elemento("cpf").value
        ),

        telefone: somenteNumeros(
            elemento("telefone").value
        ),

        email: elemento("email").value.trim(),

        secretariaId:
            elemento("secretariaId").value
                ? Number(elemento("secretariaId").value)
                : null,

        dataInicio:
            elemento("dataInicio").value || null,

        dataFim:
            elemento("dataFim").value || null
    };
}

// CARREGAR EDICAO

async function carregarEdicao(id) {
    const catequista = await requisicao(
        `${API_URL}/${id}`
    );

    elemento("catequistaId").value =
        catequista.id;

    elemento("nome").value =
        catequista.nome || "";

    elemento("cpf").value =
        mascaraCpf(catequista.cpf);

    elemento("telefone").value =
        mascaraTelefone(catequista.telefone);

    elemento("email").value =
        catequista.email || "";

    elemento("dataInicio").value =
        catequista.dataInicio || "";

    elemento("dataFim").value =
        catequista.dataFim || "";

    // A API preserva a secretaria existente
    // durante a alteração.

    elemento("secretariaId").value = "";
    elemento("secretariaId").disabled = true;

    elemento("tituloFormulario").textContent =
        "Editar Catequista";

    elemento("caminhoAtual").textContent =
        "Editar Catequista";

    elemento("btnSalvar").textContent =
        "Salvar alterações";
}

// SALVAR CADASTRO OU EDICAO

async function salvarCadastro(evento) {
    evento.preventDefault();

    limparMensagem();

    // Valida todos os campos antes de enviar.
    if (!validarFormulario()) {
        return;
    }

    const id = elemento("catequistaId").value;
    const botao = elemento("btnSalvar");

    try {
        const dados = obterDados();

        botao.disabled = true;

        await requisicao(
            id ? `${API_URL}/${id}` : API_URL,
            {
                method: id ? "PUT" : "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify(dados)
            }
        );

        window.location.href =
            "/consultar-catequistas.html";

    } catch (erro) {
        mostrarErroFormulario(erro);

    } finally {
        botao.disabled = false;
    }
}

// INICIALIZAR PAGINA DE CADASTRO

function iniciarCadastro() {
    const formulario = elemento("formCatequista");

    const parametros = new URLSearchParams(
        window.location.search
    );

    const id = parametros.get("id");

    const cpf = elemento("cpf");
    const telefone = elemento("telefone");

    cpf.addEventListener("input", () => {
        cpf.value = mascaraCpf(cpf.value);
    });

    telefone.addEventListener("input", () => {
        telefone.value = mascaraTelefone(
            telefone.value
        );
    });

    // Atualiza os erros conforme o usuario corrige.

    formulario.querySelectorAll("input").forEach(campo => {

        campo.addEventListener("input", () => {
            limparErroCampo(campo);

            if (formulario.classList.contains(
                "tentou-enviar"
            )) {
                validarCampoIndividual(campo);
            }
        });

        campo.addEventListener("change", () => {
            limparErroCampo(campo);

            if (formulario.classList.contains(
                "tentou-enviar"
            )) {
                validarCampoIndividual(campo);
            }
        });
    });

    formulario.addEventListener(
        "submit",
        salvarCadastro
    );

    elemento("btnLimpar").addEventListener(
        "click",
        () => {

            if (id) {
                window.location.reload();

            } else {
                formulario.reset();

                formulario.classList.remove(
                    "tentou-enviar"
                );

                limparTodosErros();
                limparMensagem();
            }
        }
    );

    if (id) {
        if (!/^[1-9]\d*$/.test(id)) {
            mostrarMensagem(
                "ID do catequista inválido."
            );

            formulario.querySelectorAll(
                "input, button"
            ).forEach(campo => {
                campo.disabled = true;
            });

            return;
        }

        carregarEdicao(id).catch(erro => {
            mostrarMensagem(erro.message);

            formulario.querySelectorAll(
                "input, button"
            ).forEach(campo => {
                campo.disabled = true;
            });
        });
    }
}

// VALIDAR CAMPO APOS CORRECAO

function validarCampoIndividual(campo) {
    const id = campo.id;
    const valor = campo.value.trim();

    let erro = "";

    if (id === "nome" && !valor) {
        erro = "Nome obrigatório.";
    }

    if (id === "cpf") {
        if (!somenteNumeros(valor)) {
            erro = "CPF obrigatório.";
        } else if (!cpfValido(valor)) {
            erro = "Informe um CPF válido.";
        }
    }

    if (id === "telefone") {
        const numeros = somenteNumeros(valor);

        if (numeros &&
            numeros.length !== 10 &&
            numeros.length !== 11) {

            erro = "Telefone deve ter 10 ou 11 números.";
        }
    }

    if (id === "email" &&
        valor &&
        !campo.checkValidity()) {

        erro = "Informe um e-mail válido.";
    }

    if (id === "dataFim") {
        const inicio = elemento("dataInicio").value;

        if (inicio && valor && valor < inicio) {
            erro = "Data fim não pode ser anterior à data início.";
        }
    }

    if (!erro && !campo.checkValidity()) {
        erro = "Preencha corretamente este campo.";
    }

    if (erro) {
        marcarErro(campo, erro);
    }

    // Se dataInicio mudou, revalidar dataFim.
    if (id === "dataInicio") {
        const fim = elemento("dataFim");

        limparErroCampo(fim);
        validarCampoIndividual(fim);
    }
}

// CONSULTA DE CATEQUISTAS

function criarCelula(linha, valor) {
    const celula = document.createElement("td");

    celula.textContent =
        valor === null ||
        valor === undefined ||
        valor === ""
            ? "-"
            : String(valor);

    linha.appendChild(celula);
}

function renderizarTabela(catequistas) {
    const tabela = elemento("tabelaCatequistas");

    tabela.replaceChildren();

    if (!catequistas.length) {
        const linha = document.createElement("tr");
        const celula = document.createElement("td");

        celula.colSpan = 7;
        celula.textContent =
            "Nenhum catequista encontrado.";

        linha.appendChild(celula);
        tabela.appendChild(linha);

        return;
    }

    catequistas.forEach(c => {
        const linha = document.createElement("tr");

        // ID utilizado internamente, sem exibir coluna.
        criarCelula(linha, c.nome);
        criarCelula(linha, mascaraCpf(c.cpf));
        criarCelula(linha, mascaraTelefone(c.telefone));
        criarCelula(linha, c.email);
        criarCelula(linha, formatarData(c.dataInicio));
        criarCelula(linha, formatarData(c.dataFim));

        const acoes = document.createElement("td");

        const editar = document.createElement("button");

        editar.type = "button";
        editar.className = "btn-editar";
        editar.textContent = "Editar";

        editar.addEventListener("click", () => {
            window.location.href =
                `/cadastrar-catequista.html?id=${c.id}`;
        });

        const excluir = document.createElement("button");

        excluir.type = "button";
        excluir.className = "btn-excluir";
        excluir.textContent = "Excluir";

        excluir.addEventListener("click", () => {
            excluirCatequista(c.id, c.nome);
        });

        acoes.append(editar, excluir);

        linha.appendChild(acoes);
        tabela.appendChild(linha);
    });
}

async function listarCatequistas() {
    try {
        const lista = await requisicao(API_URL);

        renderizarTabela(lista);

    } catch (erro) {
        mostrarMensagem(erro.message);
    }
}

async function pesquisarCatequistas() {
    limparMensagem();

    const tipo = elemento("tipoBusca").value;
    const valor = elemento("valorBusca").value.trim();

    if (!valor) {
        await listarCatequistas();
        return;
    }

    try {
        let lista;

        if (tipo === "cpf") {
            const cpf = somenteNumeros(valor);

            if (!cpf) {
                throw new Error(
                    "Informe ao menos um número do CPF."
                );
            }

            // Pesquisa parcial: retorna uma lista.

            lista = await requisicao(
                `${API_URL}/cpf?cpf=${encodeURIComponent(cpf)}`
            );

        } else {
            lista = await requisicao(
                `${API_URL}/nome?nome=${encodeURIComponent(valor)}`
            );
        }

        renderizarTabela(lista);

        if (!lista.length) {
            mostrarMensagem(
                "Nenhum catequista encontrado."
            );
        }

    } catch (erro) {
        mostrarMensagem(erro.message);
    }
}

// EXCLUIR CATEQUISTA

async function excluirCatequista(id, nome) {
    if (!confirm(
        `Deseja excluir o catequista "${nome}"?`
    )) {
        return;
    }

    try {
        await requisicao(
            `${API_URL}/${id}`,
            {
                method: "DELETE"
            }
        );

        await listarCatequistas();

        mostrarMensagem(
            "Catequista excluído com sucesso!",
            "sucesso"
        );

    } catch (erro) {
        mostrarMensagem(erro.message);
    }
}

// INICIAR CONSULTA

function iniciarConsulta() {
    const tipo = elemento("tipoBusca");
    const valor = elemento("valorBusca");

    tipo.addEventListener("change", () => {
        valor.value = "";

        if (tipo.value === "cpf") {
            valor.placeholder = "000.000.000-00";
            valor.maxLength = 14;
            valor.inputMode = "numeric";

        } else {
            valor.placeholder = "Digite o nome";
            valor.removeAttribute("maxlength");
            valor.inputMode = "text";
        }
    });

    valor.addEventListener("input", () => {
        if (tipo.value === "cpf") {
            valor.value = mascaraCpf(valor.value);
        }
    });

    valor.addEventListener("keydown", evento => {
        if (evento.key === "Enter") {
            evento.preventDefault();

            pesquisarCatequistas();
        }
    });

    elemento("btnPesquisar").addEventListener(
        "click",
        pesquisarCatequistas
    );

    elemento("btnListar").addEventListener(
        "click",
        () => {
            valor.value = "";

            limparMensagem();
            listarCatequistas();
        }
    );

    listarCatequistas();
}

// IDENTIFICAR PAGINA

if (pagina === "cadastro") {
    iniciarCadastro();
}

if (pagina === "consulta") {
    iniciarConsulta();
}