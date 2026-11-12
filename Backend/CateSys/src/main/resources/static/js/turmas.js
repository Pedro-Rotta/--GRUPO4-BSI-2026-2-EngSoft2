
document.addEventListener("DOMContentLoaded", () => {

    const API_TURMAS = "/api/turmas";

    const tabela = document.getElementById("tabelaTurmas");
    const mensagem = document.getElementById("mensagemTurmas");
    const btnAtualizar = document.getElementById("btnAtualizar");

    function mostrarMensagem(texto, tipo = "danger") {
        mensagem.className = `alert alert-${tipo} mt-3`;
        mensagem.textContent = texto;
    }

    function limparMensagem() {
        mensagem.className = "";
        mensagem.textContent = "";
    }

    function criarCelula(linha, valor) {
        const celula = document.createElement("td");

        celula.textContent =
            valor === null || valor === undefined || valor === ""
                ? "-"
                : String(valor);

        linha.appendChild(celula);
        return celula;
    }

    function mostrarVazio(texto) {
        tabela.replaceChildren();

        const linha = document.createElement("tr");
        const celula = document.createElement("td");

        celula.colSpan = 5;
        celula.className = "text-center text-muted py-4";
        celula.textContent = texto;

        linha.appendChild(celula);
        tabela.appendChild(linha);
    }

    function formatarEtapa(etapa) {
        const nomes = {
            EUCARISTIA_1: "Eucaristia 1",
            EUCARISTIA_2: "Eucaristia 2",
            CRISMA_1: "Crisma 1",
            CRISMA_2: "Crisma 2"
        };

        return nomes[etapa] || etapa || "-";
    }

    function formatarStatus(status) {
        const nomes = {
            ABERTA: "Aberta",
            EM_ANDAMENTO: "Em andamento",
            ENCERRADA: "Encerrada",
            CANCELADA: "Cancelada"
        };

        return nomes[status] || status || "-";
    }

    function renderizarTurmas(turmas) {
        tabela.replaceChildren();

        if (turmas.length === 0) {
            mostrarVazio("Nenhuma turma cadastrada.");
            return;
        }

        turmas.forEach(turma => {

            const linha = document.createElement("tr");

            criarCelula(linha, turma.id);
            criarCelula(linha, formatarEtapa(turma.etapa));
            criarCelula(linha, turma.anoOferta);
            criarCelula(linha, formatarStatus(turma.status));

            const acoes = document.createElement("td");

            const link = document.createElement("a");

            link.href =
                "/html/trocar-catequista-turma.html";

            link.className = "btn btn-sm btn-outline-primary";
            link.textContent = "Trocar Catequista";

            acoes.appendChild(link);
            linha.appendChild(acoes);

            tabela.appendChild(linha);
        });
    }

    async function carregarTurmas() {
        limparMensagem();
        btnAtualizar.disabled = true;

        mostrarVazio("Carregando turmas...");

        try {
            const resposta = await fetch(API_TURMAS);

            if (!resposta.ok) {
                throw new Error(
                    `Erro ao consultar turmas (HTTP ${resposta.status}).`
                );
            }

            const turmas = await resposta.json();

            if (!Array.isArray(turmas)) {
                throw new Error(
                    "A API não retornou uma lista de turmas."
                );
            }

            renderizarTurmas(turmas);

        } catch (erro) {
            mostrarVazio("Não foi possível carregar as turmas.");
            mostrarMensagem(erro.message);

        } finally {
            btnAtualizar.disabled = false;
        }
    }

    btnAtualizar.addEventListener(
        "click",
        carregarTurmas
    );

    carregarTurmas();
});
