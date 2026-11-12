
(() => {
    "use strict";

    const API = "/api/catequistas";
    const $ = id => document.getElementById(id);

    const form = $("formCatequista");
    const tbody = $("tabelaCatequistas");

    if (!form || !tbody) return;

    function numeros(valor) {
        return String(valor ?? "").replace(/\D/g, "");
    }

    function mascaraCpf(valor) {
        const n = numeros(valor).slice(0, 11);

        return n
            .replace(/^(\d{3})(\d)/, "$1.$2")
            .replace(/^(\d{3})\.(\d{3})(\d)/, "$1.$2.$3")
            .replace(/\.(\d{3})(\d)/, ".$1-$2");
    }

    function mascaraTelefone(valor) {
        const n = numeros(valor).slice(0, 11);

        if (!n) return "";
        if (n.length <= 2) return `(${n}`;

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
        const cpf = numeros(valor);

        if (!/^\d{11}$/.test(cpf)) return false;
        if (/^(\d)\1{10}$/.test(cpf)) return false;

        for (let tamanho = 9; tamanho <= 10; tamanho++) {
            let soma = 0;

            for (let i = 0; i < tamanho; i++) {
                soma += Number(cpf[i]) * (tamanho + 1 - i);
            }

            let resto = (soma * 10) % 11;

            if (resto === 10) resto = 0;

            if (resto !== Number(cpf[tamanho])) {
                return false;
            }
        }

        return true;
    }

    function dataFormatada(data) {
        if (!data) return "—";

        const p = data.split("-");

        return p.length === 3
            ? `${p[2]}/${p[1]}/${p[0]}`
            : data;
    }

    async function api(url, opcoes = {}) {
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
                // Resposta de erro em texto simples.
            }

            throw new Error(mensagem);
        }

        if (!texto) return null;

        return JSON.parse(texto);
    }

    function mostrarMensagem(id, texto, erro = false) {
        const area = $(id);

        area.textContent = texto;

        area.className = erro
            ? "mt-3 text-danger"
            : "mt-3 text-success";
    }

    function criarCelula(linha, valor) {
        const celula = document.createElement("td");

        celula.textContent =
            valor === null ||
            valor === undefined ||
            valor === ""
                ? "—"
                : String(valor);

        linha.appendChild(celula);
    }

    // LISTAGEM

    function renderizar(lista) {
        tbody.replaceChildren();

        if (!lista.length) {
            const linha = document.createElement("tr");
            const celula = document.createElement("td");

            celula.colSpan = 6;
            celula.className = "text-center text-muted py-4";
            celula.textContent =
                "Nenhum catequista encontrado.";

            linha.appendChild(celula);
            tbody.appendChild(linha);

            return;
        }

        lista.forEach(c => {
            const linha = document.createElement("tr");

            criarCelula(linha, c.nome);
            criarCelula(linha, mascaraCpf(c.cpf));

            const contato = [
                mascaraTelefone(c.telefone),
                c.email
            ].filter(Boolean).join(" / ");

            criarCelula(linha, contato);
            criarCelula(linha, dataFormatada(c.dataInicio));
            criarCelula(linha, dataFormatada(c.dataFim));

            const acoes = document.createElement("td");

            const editar = document.createElement("button");
            editar.type = "button";
            editar.className =
                "btn btn-sm btn-outline-secondary me-2";
            editar.textContent = "Editar";

            editar.addEventListener("click", () => {
                editarCatequista(c.id);
            });

            const excluir = document.createElement("button");
            excluir.type = "button";
            excluir.className =
                "btn btn-sm btn-outline-danger";
            excluir.textContent = "Excluir";

            excluir.addEventListener("click", () => {
                excluirCatequista(c.id, c.nome);
            });

            acoes.append(editar, excluir);

            linha.appendChild(acoes);
            tbody.appendChild(linha);
        });
    }

    // PESQUISA POR NOME OU CPF PARCIAL

    async function listar() {
        const tipo = $("tipoBusca").value;
        const entrada = $("buscaCatequista").value.trim();

        const busca = tipo === "cpf"
            ? numeros(entrada)
            : entrada;

        if (entrada && !busca) {
            renderizar([]);

            mostrarMensagem(
                "mensagemCatequistas",
                "Informe ao menos um número do CPF.",
                true
            );

            return;
        }

        const url = !busca
            ? API
            : `${API}/${tipo}?${tipo}=${encodeURIComponent(busca)}`;

        try {
            const lista = await api(url);

            if (!Array.isArray(lista)) {
                throw new Error(
                    "A API deve retornar uma lista."
                );
            }

            renderizar(lista);

            mostrarMensagem(
                "mensagemCatequistas",
                ""
            );

        } catch (erro) {
            renderizar([]);

            mostrarMensagem(
                "mensagemCatequistas",
                erro.message,
                true
            );
        }
    }

    // VALIDACAO VISUAL

    function limparErros() {
        form.querySelectorAll(".campo-invalido")
            .forEach(campo => {
                campo.classList.remove("campo-invalido");
                campo.removeAttribute("aria-invalid");
            });

        form.querySelectorAll(".erro-campo")
            .forEach(erro => erro.remove());
    }

    function marcarErro(campo, texto) {
        campo.classList.add("campo-invalido");
        campo.setAttribute("aria-invalid", "true");

        const aviso = document.createElement("div");
        aviso.className = "erro-campo";
        aviso.textContent = texto;

        campo.insertAdjacentElement("afterend", aviso);
    }

    function validar() {
        form.classList.add("tentou-enviar");

        limparErros();

        let primeiroInvalido = null;

        function erro(id, texto) {
            const campo = $(id);

            if (campo.classList.contains("campo-invalido")) {
                return;
            }

            marcarErro(campo, texto);

            if (!primeiroInvalido) {
                primeiroInvalido = campo;
            }
        }

        // Campos obrigatorios

        if (!$("nome").value.trim()) {
            erro("nome", "Nome obrigatório.");
        }

        if (!numeros($("cpf").value)) {
            erro("cpf", "CPF obrigatório.");

        } else if (!cpfValido($("cpf").value)) {
            erro("cpf", "CPF inválido.");
        }

        // Campos opcionais, mas validados quando preenchidos

        const telefone = numeros($("telefone").value);

        if (telefone && ![10, 11].includes(telefone.length)) {
            erro("telefone", "Telefone inválido.");
        }

        if ($("email").value &&
            !$("email").checkValidity()) {

            erro("email", "E-mail inválido.");
        }

        const inicio = $("dataInicio").value;
        const fim = $("dataFim").value;

        if (inicio && !$("dataInicio").checkValidity()) {
            erro("dataInicio", "Data inicial inválida.");
        }

        if (fim && !$("dataFim").checkValidity()) {
            erro("dataFim", "Data final inválida.");
        }

        if (inicio && fim && fim < inicio) {
            erro(
                "dataFim",
                "Data final anterior à data inicial."
            );
        }

        if (!$("secretariaId").disabled &&
            !$("secretariaId").checkValidity()) {

            erro(
                "secretariaId",
                "ID de secretaria inválido."
            );
        }

        // Verificacao complementar de regras HTML5

        form.querySelectorAll(
            "input:not([type='hidden']):not(:disabled)"
        ).forEach(campo => {

            if (!campo.checkValidity() &&
                !campo.classList.contains("campo-invalido")) {

                erro(
                    campo.id,
                    "Preencha corretamente este campo."
                );
            }
        });

        if (primeiroInvalido) {
            primeiroInvalido.focus();

            mostrarMensagem(
                "mensagemFormulario",
                "Corrija os campos destacados.",
                true
            );

            return false;
        }

        mostrarMensagem("mensagemFormulario", "");
        return true;
    }

    function obterDados() {
        return {
            nome: $("nome").value.trim(),
            cpf: numeros($("cpf").value),
            telefone: numeros($("telefone").value),
            email: $("email").value.trim(),

            secretariaId: $("secretariaId").value
                ? Number($("secretariaId").value)
                : null,

            dataInicio: $("dataInicio").value || null,
            dataFim: $("dataFim").value || null
        };
    }

    function limparFormulario() {
        form.reset();

        $("catequistaId").value = "";
        $("secretariaId").disabled = false;

        $("tituloFormulario").textContent =
            "Cadastro de catequista";

        $("btnSalvar").textContent =
            "Salvar cadastro";

        form.classList.remove("tentou-enviar");

        limparErros();

        mostrarMensagem("mensagemFormulario", "");
    }

    // EDICAO

    async function editarCatequista(id) {
        try {
            const c = await api(`${API}/${id}`);

            $("catequistaId").value = c.id;
            $("nome").value = c.nome || "";
            $("cpf").value = mascaraCpf(c.cpf);

            $("telefone").value =
                mascaraTelefone(c.telefone);

            $("email").value = c.email || "";
            $("dataInicio").value = c.dataInicio || "";
            $("dataFim").value = c.dataFim || "";

            // A API atual nao retorna secretariaId.
            // O backend preserva a secretaria na edicao.

            $("secretariaId").value = "";
            $("secretariaId").disabled = true;

            $("tituloFormulario").textContent =
                "Editar catequista";

            $("btnSalvar").textContent =
                "Salvar alterações";

            form.classList.remove("tentou-enviar");
            limparErros();

            mostrarMensagem("mensagemFormulario", "");

            $("cadastro").scrollIntoView({
                behavior: "smooth"
            });

        } catch (erro) {
            mostrarMensagem(
                "mensagemCatequistas",
                erro.message,
                true
            );
        }
    }

    // EXCLUSAO

    async function excluirCatequista(id, nome) {
        if (!confirm(
            `Deseja excluir o catequista "${nome}"?`
        )) {
            return;
        }

        try {
            await api(`${API}/${id}`, {
                method: "DELETE"
            });

            await listar();

            mostrarMensagem(
                "mensagemCatequistas",
                "Catequista excluído com sucesso."
            );

        } catch (erro) {
            mostrarMensagem(
                "mensagemCatequistas",
                erro.message,
                true
            );
        }
    }

    // SALVAR

    form.addEventListener("submit", async evento => {
        evento.preventDefault();

        if (!validar()) return;

        const id = $("catequistaId").value;
        const botao = $("btnSalvar");

        botao.disabled = true;

        try {
            await api(
                id ? `${API}/${id}` : API,
                {
                    method: id ? "PUT" : "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify(obterDados())
                }
            );

            limparFormulario();
            await listar();

            mostrarMensagem(
                "mensagemCatequistas",
                id
                    ? "Catequista atualizado com sucesso."
                    : "Catequista cadastrado com sucesso."
            );

        } catch (erro) {
            mostrarMensagem(
                "mensagemFormulario",
                erro.message,
                true
            );

        } finally {
            botao.disabled = false;
        }
    });

    // EVENTOS DO FORMULARIO

    $("btnCancelar").addEventListener(
        "click",
        limparFormulario
    );

    $("cpf").addEventListener("input", evento => {
        evento.target.value =
            mascaraCpf(evento.target.value);
    });

    $("telefone").addEventListener("input", evento => {
        evento.target.value =
            mascaraTelefone(evento.target.value);
    });

    form.querySelectorAll("input").forEach(campo => {

        campo.addEventListener("input", () => {
            campo.classList.remove("campo-invalido");
            campo.removeAttribute("aria-invalid");

            const proximo = campo.nextElementSibling;

            if (proximo?.classList.contains("erro-campo")) {
                proximo.remove();
            }
        });
    });

    // EVENTOS DA PESQUISA

    $("buscaCatequista").addEventListener(
        "input",
        evento => {

            if ($("tipoBusca").value === "cpf") {
                evento.target.value =
                    mascaraCpf(evento.target.value);
            }
        }
    );

    $("tipoBusca").addEventListener("change", () => {
        $("buscaCatequista").value = "";
        listar();
    });

    $("buscaCatequista").addEventListener(
        "keydown",
        evento => {

            if (evento.key === "Enter") {
                evento.preventDefault();
                listar();
            }
        }
    );

    $("buscaCatequista").addEventListener(
        "search",
        listar
    );

    let temporizador;

    $("buscaCatequista").addEventListener(
        "input",
        () => {
            clearTimeout(temporizador);

            temporizador = setTimeout(
                listar,
                300
            );
        }
    );

    listar();

})();
