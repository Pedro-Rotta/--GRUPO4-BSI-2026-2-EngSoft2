
document.addEventListener("DOMContentLoaded", () => {

    const API_TURMAS = "/api/turmas";
    const API_CATEQUISTAS = "/api/catequistas";

    const el = id => document.getElementById(id);

    const turmaSelect = el("turmaId");
    const atualSelect = el("catequistaAtualId");
    const novoSelect = el("novoCatequistaId");

    const btnBuscar = el("btnBuscarTurma");
    const btnConfirmar = el("btnConfirmar");
    const btnCancelar = el("btnCancelar");

    const secaoTurma = el("secaoTurma");
    const secaoTroca = el("secaoTroca");
    const formulario = el("formTroca");

    let turmaAtual = null;
    let catequistas = [];
    let enviando = false;

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
                const json = JSON.parse(texto);

                mensagem = json.message ||
                    json.detail ||
                    json.error ||
                    mensagem;
            } catch {
                // O erro pode vir como texto simples.
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

        area.className = `alert alert-${tipo}`;
        area.textContent = texto;
        area.hidden = false;
    }

    function limparMensagem() {

        const area = el("mensagem");

        area.className = "";
        area.textContent = "";
        area.hidden = true;
    }

    // FORMATACOES

    function formatarCpf(valor) {

        const cpf = String(valor || "")
            .replace(/\D/g, "");

        if (cpf.length !== 11) {
            return valor || "-";
        }

        return cpf.replace(
            /(\d{3})(\d{3})(\d{3})(\d{2})/,
            "$1.$2.$3-$4"
        );
    }

    function definirTexto(id, valor) {

        el(id).textContent =
            valor === null ||
            valor === undefined ||
            valor === ""
                ? "-"
                : String(valor);
    }

    function nomeTurma(turma) {

        if (turma.nome) {
            return turma.nome;
        }

        return [
            turma.etapa || "Turma",
            turma.anoOferta
        ].filter(Boolean).join(" - ");
    }

    function buscarCatequista(id) {

        return catequistas.find(
            c => Number(c.id) === Number(id)
        );
    }

    function nomeCatequista(id) {

        const catequista = buscarCatequista(id);

        return catequista
            ? catequista.nome
            : `Catequista ID ${id}`;
    }

    // CONTROLES

    function opcaoInicial(select, mensagem) {

        select.replaceChildren();

        const opcao = document.createElement("option");

        opcao.value = "";
        opcao.textContent = mensagem;

        select.appendChild(opcao);
    }

    function adicionarOpcao(select, id, texto) {

        const opcao = document.createElement("option");

        opcao.value = id;
        opcao.textContent = texto;

        select.appendChild(opcao);
    }

    function limparSelecao() {

        turmaAtual = null;

        secaoTurma.hidden = true;
        secaoTroca.hidden = true;

        opcaoInicial(
            atualSelect,
            "Selecione o catequista atual"
        );

        opcaoInicial(
            novoSelect,
            "Selecione o novo catequista"
        );

        definirTexto(
            "nomeCatequistaAtual",
            "Nenhum selecionado"
        );

        definirTexto("cpfCatequistaAtual", "");

        formulario.classList.remove("tentou-enviar");

        atualSelect.classList.remove("is-invalid");
        novoSelect.classList.remove("is-invalid");

        atualSelect.disabled = true;
        novoSelect.disabled = true;

        btnConfirmar.disabled = true;
    }

    function atualizarBotao() {

        if (enviando) {
            btnConfirmar.disabled = true;
            return;
        }

        const atualId = Number(atualSelect.value);
        const novoId = Number(novoSelect.value);

        btnConfirmar.disabled =
            !turmaAtual ||
            !atualId ||
            !novoId ||
            atualId === novoId;
    }

    // LISTAR TURMAS

    async function carregarTurmas() {

        turmaSelect.disabled = true;
        btnBuscar.disabled = true;

        try {

            const turmas = await requisicao(API_TURMAS);

            if (!Array.isArray(turmas)) {
                throw new Error(
                    "A API de turmas não retornou uma lista."
                );
            }

            opcaoInicial(
                turmaSelect,
                "Selecione a turma"
            );

            turmas.forEach(turma => {

                adicionarOpcao(
                    turmaSelect,
                    turma.id,
                    nomeTurma(turma)
                );
            });

            turmaSelect.disabled = turmas.length === 0;

            if (turmas.length === 0) {
                mostrarMensagem(
                    "Nenhuma turma cadastrada foi encontrada.",
                    "warning"
                );
            }

        } catch (erro) {

            opcaoInicial(
                turmaSelect,
                "Erro ao carregar turmas"
            );

            mostrarMensagem(erro.message);
        }
    }

    // LISTAR CATEQUISTAS

    async function carregarCatequistas() {

        const lista = await requisicao(API_CATEQUISTAS);

        if (!Array.isArray(lista)) {
            throw new Error(
                "A API de catequistas não retornou uma lista."
            );
        }

        catequistas = lista;
    }

    // MOSTRAR CATEQUISTAS DA TURMA

    function preencherCatequistasAtuais(turma) {

        opcaoInicial(
            atualSelect,
            "Selecione o catequista que será substituído"
        );

        const ids = turma.catequistasIds || [];

        if (!Array.isArray(ids)) {
            throw new Error(
                "A API não retornou os vínculos da turma."
            );
        }

        ids.forEach(id => {

            adicionarOpcao(
                atualSelect,
                id,
                nomeCatequista(id)
            );
        });

        atualSelect.disabled = ids.length === 0;

        if (ids.length === 0) {
            mostrarMensagem(
                "Esta turma não possui catequistas vinculados.",
                "warning"
            );
        }
    }

    // MOSTRAR DADOS DO CATEQUISTA SELECIONADO

    function exibirCatequistaAtual() {

        const id = Number(atualSelect.value);

        if (!id) {

            definirTexto(
                "nomeCatequistaAtual",
                "Nenhum selecionado"
            );

            definirTexto("cpfCatequistaAtual", "");
            return;
        }

        const catequista = buscarCatequista(id);

        definirTexto(
            "nomeCatequistaAtual",
            catequista ? catequista.nome : `ID ${id}`
        );

        definirTexto(
            "cpfCatequistaAtual",
            catequista ? formatarCpf(catequista.cpf) : ""
        );
    }

    // LISTAR POSSIVEIS SUBSTITUTOS

    function preencherNovosCatequistas() {

        opcaoInicial(
            novoSelect,
            "Selecione o novo catequista"
        );

        if (!turmaAtual) {
            novoSelect.disabled = true;
            return;
        }

        const atuais = turmaAtual.catequistasIds || [];

        catequistas.forEach(catequista => {

            // Nao oferece pessoas que ja estao na turma.

            if (atuais.some(
                id => Number(id) === Number(catequista.id)
            )) {
                return;
            }

            adicionarOpcao(
                novoSelect,
                catequista.id,
                `${catequista.nome} - ${formatarCpf(catequista.cpf)}`
            );
        });

        novoSelect.disabled =
            novoSelect.options.length <= 1;

        atualizarBotao();
    }

    // MOSTRAR DETALHES DA TURMA

    function exibirTurma(turma) {

        turmaAtual = turma;

        definirTexto(
            "nomeTurma",
            nomeTurma(turma)
        );

        definirTexto(
            "statusTurma",
            turma.status
        );

        // A API atual devolve os IDs de horario e local.
        // Nao inventamos dia, hora ou nome da sala.

        definirTexto(
            "horarioTurma",
            turma.horarioDescricao ||
            (
                turma.horarioId
                    ? `Horário ID ${turma.horarioId}`
                    : "Não informado"
            )
        );

        definirTexto(
            "localTurma",
            turma.localNome ||
            (
                turma.localId
                    ? `Local ID ${turma.localId}`
                    : "Não informado"
            )
        );

        preencherCatequistasAtuais(turma);
        preencherNovosCatequistas();
        exibirCatequistaAtual();

        secaoTurma.hidden = false;
        secaoTroca.hidden = false;
    }

    // BUSCAR TURMA

    async function buscarTurma() {

        const id = turmaSelect.value;

        limparMensagem();
        limparSelecao();

        if (!id) {
            mostrarMensagem(
                "Selecione uma turma.",
                "warning"
            );
            return;
        }

        btnBuscar.disabled = true;

        try {

            const turma = await requisicao(
                `${API_TURMAS}/${encodeURIComponent(id)}`
            );

            if (!turma || !turma.id) {
                throw new Error(
                    "Turma não encontrada."
                );
            }

            await carregarCatequistas();

            exibirTurma(turma);

        } catch (erro) {

            limparSelecao();
            mostrarMensagem(erro.message);

        } finally {

            btnBuscar.disabled = !turmaSelect.value;
        }
    }

    // VALIDAR A TROCA

    function validarTroca() {

        formulario.classList.add("tentou-enviar");

        atualSelect.classList.remove("is-invalid");
        novoSelect.classList.remove("is-invalid");

        if (!turmaAtual) {
            mostrarMensagem(
                "Primeiro selecione uma turma.",
                "warning"
            );
            return false;
        }

        const atualId = Number(atualSelect.value);
        const novoId = Number(novoSelect.value);

        if (!atualId) {

            atualSelect.classList.add("is-invalid");

            mostrarMensagem(
                "Selecione quem será substituído.",
                "warning"
            );

            return false;
        }

        if (!novoId) {

            novoSelect.classList.add("is-invalid");

            mostrarMensagem(
                "Selecione o novo catequista.",
                "warning"
            );

            return false;
        }

        const idsAtuais = turmaAtual.catequistasIds || [];

        if (!idsAtuais.some(id => Number(id) === atualId)) {

            mostrarMensagem(
                "O catequista atual não está vinculado à turma."
            );

            return false;
        }

        if (idsAtuais.some(id => Number(id) === novoId)) {

            mostrarMensagem(
                "O novo catequista já participa desta turma.",
                "warning"
            );

            return false;
        }

        if (!buscarCatequista(novoId)) {

            mostrarMensagem(
                "Novo catequista não encontrado."
            );

            return false;
        }

        return true;
    }

    // CONFIRMAR A TROCA

    async function confirmarTroca(evento) {

        evento.preventDefault();
        limparMensagem();

        if (!validarTroca()) {
            return;
        }

        const atualId = Number(atualSelect.value);
        const novoId = Number(novoSelect.value);

        const confirmou = confirm(
            `Deseja substituir "${nomeCatequista(atualId)}" ` +
            `por "${nomeCatequista(novoId)}" ` +
            `na turma "${nomeTurma(turmaAtual)}"?`
        );

        if (!confirmou) {
            return;
        }

        enviando = true;
        atualizarBotao();
        btnBuscar.disabled = true;

        try {

            const idTurma = turmaAtual.id;

            // Esta rota sera implementada na proxima
            // etapa do backend.

            await requisicao(
                `${API_TURMAS}/${idTurma}/catequista`,
                {
                    method: "PUT",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({
                        catequistaAtualId: atualId,
                        novoCatequistaId: novoId
                    })
                }
            );

            // Recarrega o estado real do banco.

            const atualizada = await requisicao(
                `${API_TURMAS}/${idTurma}`
            );

            exibirTurma(atualizada);

            const ids = atualizada.catequistasIds || [];

            if (
                !ids.some(id => Number(id) === novoId) ||
                ids.some(id => Number(id) === atualId)
            ) {
                throw new Error(
                    "A troca não foi confirmada na consulta."
                );
            }

            mostrarMensagem(
                "Catequista substituído com sucesso!",
                "success"
            );

        } catch (erro) {

            mostrarMensagem(erro.message);

        } finally {

            enviando = false;
            btnBuscar.disabled = !turmaSelect.value;
            atualizarBotao();
        }
    }

    // EVENTOS

    turmaSelect.addEventListener("change", () => {

        limparSelecao();
        limparMensagem();

        btnBuscar.disabled = !turmaSelect.value;
    });

    btnBuscar.addEventListener("click", buscarTurma);

    atualSelect.addEventListener("change", () => {

        atualSelect.classList.remove("is-invalid");

        exibirCatequistaAtual();
        atualizarBotao();
    });

    novoSelect.addEventListener("change", () => {

        novoSelect.classList.remove("is-invalid");

        atualizarBotao();
    });

    btnCancelar.addEventListener("click", () => {

        turmaSelect.value = "";

        limparSelecao();
        limparMensagem();

        btnBuscar.disabled = true;
    });

    formulario.addEventListener(
        "submit",
        confirmarTroca
    );

    // INICIALIZAR

    limparSelecao();
    limparMensagem();
    carregarTurmas();

});
