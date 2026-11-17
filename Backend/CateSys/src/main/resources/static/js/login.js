// Tela de login: entra com CPF ou e-mail e senha; no primeiro acesso, cadastra o administrador inicial.
(function () {
    const formLogin = document.getElementById("formLogin");
    const formPrimeiroAcesso = document.getElementById("formPrimeiroAcesso");

    function mostrar(idArea, texto, tipo = "danger") {
        const area = document.getElementById(idArea);
        area.textContent = texto;
        area.className = `alert alert-${tipo} mt-3 mb-0`;
    }

    function esconder(idArea) {
        document.getElementById(idArea).className = "alert d-none mt-3 mb-0";
    }

    function mascaraCpf(valor) {
        return valor.replace(/\D/g, "").slice(0, 11)
            .replace(/^(\d{3})(\d)/, "$1.$2")
            .replace(/^(\d{3})\.(\d{3})(\d)/, "$1.$2.$3")
            .replace(/\.(\d{3})(\d)/, ".$1-$2");
    }

    async function lerErros(resposta) {
        try {
            const json = await resposta.json();
            if (Array.isArray(json.erros)) return json.erros.join(" ");
        } catch (e) { /* resposta sem corpo */ }
        return `Erro no servidor (${resposta.status}).`;
    }

    // Guarda a sessão e segue para o sistema (a parametrização abre sozinha se ainda não existir).
    function entrar(dados) {
        localStorage.setItem("token", dados.token);
        localStorage.setItem("nomeUsuario", dados.nome);
        window.location.replace("/html/dashboard.html");
    }

    async function postar(url, corpo) {
        return fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(corpo)
        });
    }

    formLogin.addEventListener("submit", async evento => {
        evento.preventDefault();
        esconder("mensagemLogin");
        formLogin.classList.add("was-validated");
        if (!formLogin.checkValidity()) {
            mostrar("mensagemLogin", "Informe o login e a senha.", "warning");
            return;
        }
        const botao = document.getElementById("btnEntrar");
        botao.disabled = true;
        try {
            const resposta = await postar("/auth/login", {
                login: document.getElementById("loginUser").value.trim(),
                senha: document.getElementById("loginPassword").value
            });
            if (resposta.ok) entrar(await resposta.json());
            else mostrar("mensagemLogin", await lerErros(resposta));
        } catch (e) {
            mostrar("mensagemLogin", "Não foi possível conectar ao servidor.");
        } finally {
            botao.disabled = false;
        }
    });

    const cpf = document.getElementById("paCpf");
    cpf.addEventListener("input", () => cpf.value = mascaraCpf(cpf.value));

    formPrimeiroAcesso.addEventListener("submit", async evento => {
        evento.preventDefault();
        esconder("mensagemPrimeiroAcesso");
        formPrimeiroAcesso.classList.add("was-validated");
        if (!formPrimeiroAcesso.checkValidity()) {
            mostrar("mensagemPrimeiroAcesso", "Preencha os campos destacados.", "warning");
            return;
        }
        const botao = document.getElementById("btnPrimeiroAcesso");
        botao.disabled = true;
        try {
            const resposta = await postar("/auth/primeiro-acesso", {
                nome: document.getElementById("paNome").value.trim(),
                cpf: cpf.value.replace(/\D/g, ""),
                email: document.getElementById("paEmail").value.trim(),
                senha: document.getElementById("paSenha").value
            });
            if (resposta.ok) entrar(await resposta.json());
            else mostrar("mensagemPrimeiroAcesso", await lerErros(resposta));
        } catch (e) {
            mostrar("mensagemPrimeiroAcesso", "Não foi possível conectar ao servidor.");
        } finally {
            botao.disabled = false;
        }
    });

    // Sem nenhum usuário cadastrado, mostra o cadastro do administrador inicial (uso único).
    fetch("/auth/primeiro-acesso")
        .then(resposta => resposta.ok ? resposta.json() : null)
        .then(dados => {
            if (dados && dados.primeiroAcesso) {
                formLogin.classList.add("d-none");
                formPrimeiroAcesso.classList.remove("d-none");
            }
        })
        .catch(() => mostrar("mensagemLogin", "Não foi possível conectar ao servidor."));
})();
