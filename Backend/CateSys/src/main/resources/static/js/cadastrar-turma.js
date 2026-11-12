
document.addEventListener("DOMContentLoaded", () => {

    const API_TURMAS = "/api/turmas";
    const API_CATEQUISTAS = "/api/catequistas";

    const el = id => document.getElementById(id);

    const formulario = el("formTurma");
    const btnSalvar = el("btnSalvar");
    const btnLimpar = el("btnLimpar");
    const selectCatequista = el("catequistaId");

    let catequistasCarregados = false;

    // REQUISICOES HTTP

    async function requisicao(url, opcoes = {}) {

        let resposta;

        try {
            resposta = await fetch(url, opcoes);
        } catch {
            throw new Error(
                "Não foi possível conectar ao servidor."
            );
        }

        const texto = await resposta.text();

        if (!resposta.ok) {

            let mensagem = texto ||
                `Erro HTTP ${resposta.status}`;

            try {
                const dados = JSON.parse(texto);

                mensagem =
                    dados.message ||
                    dados.detail ||
                    dados.error ||
                    mensagem;

            } catch {
                // A resposta pode ser texto simples.
            }

            throw new Error(mensagem);
        }

        if (!texto) {
            return null;
        }

        return JSON.parse(texto);
    }

    // MENSAGENS

    function mostrarMensagem(texto, tipo = "danger") {

        const area = el("mensagem");

        area.className = `alert alert-${tipo} mt-3`;
        area.textContent = texto;
    }

    function limparMensagem() {

        const area = el("mensagem");

        area.textContent = "";
        area.className = "";
    }

    // CARREGAR CATEQUISTAS

    async function carregarCatequistas() {

        selectCatequista.disabled = true;
        catequistasCarregados = false;

        try {

            const lista = await requisicao(
                API_CATEQUISTAS
            );

            if (!Array.isArray(lista)) {
                throw new Error(
                    "A API não retornou uma lista de catequistas."
                );
            }

            selectCatequista.replaceChildren();

            const inicial =
                document.createElement("option");

            inicial.value = "";
            inicial.textContent =
                "Selecione um catequista";

            selectCatequista.appendChild(inicial);

            lista.forEach(catequista => {

                const opcao =
                    document.createElement("option");

                opcao.value = catequista.id;
                opcao.textContent = catequista.nome;

                selectCatequista.appendChild(opcao);
            });

            catequistasCarregados = lista.length > 0;
            selectCatequista.disabled = !catequistasCarregados;

            if (!catequistasCarregados) {

                mostrarMensagem(
                    "Nenhum catequista cadastrado. " +
                    "Cadastre um catequista antes de criar a turma.",
                    "warning"
                );
            }

        } catch (erro) {

            catequistasCarregados = false;

            selectCatequista.replaceChildren();

            const opcao =
                document.createElement("option");

            opcao.value = "";
            opcao.textContent =
                "Erro ao carregar catequistas";

            selectCatequista.appendChild(opcao);

            mostrarMensagem(erro.message);
        }
    }

    // REMOVER ERRO DE UM CAMPO

    function limparErroCampo(campo) {

        if (!campo) {
            return;
        }

        campo.classList.remove("is-invalid");
        campo.removeAttribute("aria-invalid");

        const container = campo.closest(".campo");

        if (!container) {
            return;
        }

        container.querySelectorAll(".erro-campo")
            .forEach(aviso => aviso.remove());
    }

    // REMOVER TODOS OS ERROS

    function limparErros() {

        formulario.querySelectorAll("input, select")
            .forEach(limparErroCampo);
    }

    // DESTACAR CAMPO INVALIDO

    function marcarErro(campo, mensagem) {

        if (!campo) {
            return;
        }

        limparErroCampo(campo);

        campo.classList.add("is-invalid");
        campo.setAttribute("aria-invalid", "true");

        const aviso = document.createElement("div");

        aviso.className = "erro-campo";
        aviso.textContent = mensagem;

        const container = campo.closest(".campo");

        if (container) {
            container.appendChild(aviso);
        }
    }

    // VERIFICAR UM CAMPO

    function mensagemErroCampo(campo) {

        const id = campo.id;
        const valor = campo.value.trim();

        if (campo.disabled) {

            if (id === "catequistaId") {
                return "Não existem catequistas disponíveis.";
            }

            return "";
        }

        // CAMPOS OBRIGATORIOS

        if (!valor) {

            const mensagens = {
                etapa: "Selecione uma etapa.",
                anoOferta: "Informe o ano de oferta.",
                dataInicio: "Informe a data de início.",
                dataFim: "Informe a data de término.",
                vagasTotais: "Informe o total de vagas.",
                status: "Selecione o status.",
                localId: "Informe o ID da sala.",
                horarioId: "Informe o ID do horário.",
                catequistaId: "Selecione um catequista."
            };

            return mensagens[id] ||
                "Este campo é obrigatório.";
        }

        // ANO DE OFERTA

        if (id === "anoOferta") {

            if (!/^\d{4}$/.test(valor)) {
                return "Informe um ano com 4 números.";
            }
        }

        // VAGAS

        if (id === "vagasTotais") {

            const vagas = Number(valor);

            if (!Number.isInteger(vagas) || vagas < 1) {
                return "Informe ao menos uma vaga.";
            }
        }

        // SALA E HORARIO

        if (
            id === "localId" ||
            id === "horarioId"
        ) {

            const numero = Number(valor);

            if (!Number.isInteger(numero) || numero <= 0) {

                return id === "localId"
                    ? "Informe um ID de sala válido."
                    : "Informe um ID de horário válido.";
            }
        }

        // CATEQUISTA

        if (id === "catequistaId") {

            const numero = Number(valor);

            if (!Number.isInteger(numero) || numero <= 0) {
                return "Selecione um catequista válido.";
            }
        }

        // DATAS

        if (
            id === "dataInicio" ||
            id === "dataFim"
        ) {

            if (!campo.checkValidity()) {
                return "Informe uma data válida.";
            }
        }

        if (id === "dataFim") {

            const inicio = el("dataInicio").value;

            if (inicio && valor < inicio) {

                return "A data de término não pode " +
                    "ser anterior à data de início.";
            }
        }

        // REGRAS NATIVAS DO HTML

        if (!campo.checkValidity()) {
            return "Preencha corretamente este campo.";
        }

        return "";
    }

    // VALIDAR CAMPO INDIVIDUALMENTE

    function validarCampoIndividual(campo) {

        limparErroCampo(campo);

        const mensagem = mensagemErroCampo(campo);

        if (mensagem) {

            marcarErro(campo, mensagem);

            return false;
        }

        return true;
    }

    // VALIDAR TODOS OS CAMPOS

    function validarFormulario() {

        formulario.classList.add("tentou-enviar");

        limparErros();

        let valido = true;
        let primeiroErro = null;

        const campos = formulario.querySelectorAll(
            "input, select"
        );

        campos.forEach(campo => {

            const mensagem = mensagemErroCampo(campo);

            if (mensagem) {

                marcarErro(campo, mensagem);

                valido = false;

                if (!primeiroErro && !campo.disabled) {
                    primeiroErro = campo;
                }
            }
        });

        // CONFIRMACAO DA LISTA DE CATEQUISTAS

        if (!catequistasCarregados) {

            valido = false;

            mostrarMensagem(
                "É necessário ter um catequista " +
                "cadastrado para criar a turma."
            );
        }

        // RESULTADO DA VALIDACAO

        if (!valido) {

            if (catequistasCarregados) {

                mostrarMensagem(
                    "Existem campos inválidos. " +
                    "Corrija todos os campos destacados."
                );
            }

            if (primeiroErro) {
                primeiroErro.focus();
            }

            return false;
        }

        limparMensagem();

        return true;
    }

    // OBTER DADOS DO FORMULARIO

    function obterDados() {

        const vagas = Number(
            el("vagasTotais").value
        );

        return {
            etapa: el("etapa").value,

            anoOferta: el("anoOferta").value.trim(),

            dataInicio: el("dataInicio").value,

            dataFim: el("dataFim").value,

            vagasTotais: vagas,

            vagasDisponiveis: vagas,

            status: el("status").value,

            localId: Number(
                el("localId").value
            ),

            horarioId: Number(
                el("horarioId").value
            ),

            catequistaId: Number(
                el("catequistaId").value
            )
        };
    }

    // LIMPAR FORMULARIO

    function limparFormulario() {

        formulario.reset();

        formulario.classList.remove(
            "tentou-enviar"
        );

        limparErros();
        limparMensagem();
    }

    // SALVAR TURMA

    async function salvarTurma(evento) {

        evento.preventDefault();

        limparMensagem();

        // VALIDA TODOS OS CAMPOS SIMULTANEAMENTE

        if (!validarFormulario()) {
            return;
        }

        const confirmou = confirm(
            "Deseja cadastrar esta turma?"
        );

        if (!confirmou) {
            return;
        }

        btnSalvar.disabled = true;

        try {

            const dados = obterDados();

            await requisicao(
                API_TURMAS,
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify(dados)
                }
            );

            limparFormulario();

            mostrarMensagem(
                "Turma cadastrada com sucesso!",
                "success"
            );

        } catch (erro) {

            mostrarMensagem(
                erro.message
            );

        } finally {

            btnSalvar.disabled = false;
        }
    }

    // EVENTO SALVAR

    formulario.addEventListener(
        "submit",
        salvarTurma
    );

    // EVENTO LIMPAR

    btnLimpar.addEventListener(
        "click",
        limparFormulario
    );

    // REVALIDAR OS CAMPOS QUANDO O USUARIO CORRIGE

    formulario.querySelectorAll("input, select")
        .forEach(campo => {

            function atualizarValidacao() {

                limparErroCampo(campo);

                if (
                    formulario.classList.contains(
                        "tentou-enviar"
                    )
                ) {

                    validarCampoIndividual(campo);

                    // SE A DATA INICIAL FOR ALTERADA,
                    // REVALIDAR TAMBEM A DATA FINAL.

                    if (campo.id === "dataInicio") {

                        validarCampoIndividual(
                            el("dataFim")
                        );
                    }
                }
            }

            campo.addEventListener(
                "input",
                atualizarValidacao
            );

            campo.addEventListener(
                "change",
                atualizarValidacao
            );
        });

    // INICIAR PAGINA

    carregarCatequistas();

});
