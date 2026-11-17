// Controle de acesso no front: incluir em <head> (sem defer), antes dos outros scripts da tela.
// - Sem token válido, volta para o login.
// - Coloca o token JWT em toda chamada para /api.
// - Resposta 401 (token vencido ou inválido) encerra a sessão.
// - A cada acesso, se não houver parametrização, abre a tela de parametrização.
(function () {
    const LOGIN = "/html/index.html";
    const PARAMETRIZACAO = "/html/parametrizacao.html";

    function payloadDoToken(token) {
        try {
            const base64 = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
            return JSON.parse(atob(base64));
        } catch (e) {
            return null;
        }
    }

    function tokenValido() {
        const token = localStorage.getItem("token");
        const payload = token && payloadDoToken(token);
        return !!payload && payload.exp * 1000 > Date.now();
    }

    function sair() {
        localStorage.removeItem("token");
        localStorage.removeItem("nomeUsuario");
        window.location.replace(LOGIN);
    }

    window.sair = sair;

    if (!tokenValido()) {
        sair();
        return;
    }

    // Toda chamada para a API leva o token no cabeçalho Authorization.
    const fetchOriginal = window.fetch.bind(window);
    window.fetch = async function (url, opcoes = {}) {
        const endereco = typeof url === "string" ? url : url.url;
        const ehApi = new URL(endereco, window.location.origin).pathname.startsWith("/api/");
        if (ehApi) {
            const cabecalhos = new Headers(opcoes.headers || {});
            cabecalhos.set("Authorization", "Bearer " + localStorage.getItem("token"));
            opcoes = { ...opcoes, headers: cabecalhos };
        }
        const resposta = await fetchOriginal(url, opcoes);
        if (ehApi && resposta.status === 401) sair();
        return resposta;
    };

    // Sem parametrização, a tela de parametrização abre automaticamente.
    if (window.location.pathname !== PARAMETRIZACAO) {
        fetchOriginal("/api/parametrizacao")
            .then(resposta => {
                if (resposta.status === 404) window.location.replace(PARAMETRIZACAO);
            })
            .catch(() => { /* sem servidor: a própria tela mostra o erro */ });
    }

    // Botão "Sair" (o menu é montado depois, por isso a delegação no document).
    document.addEventListener("click", evento => {
        if (evento.target.closest(".btn-logout")) {
            evento.preventDefault();
            sair();
        }
    });

    // Nome e iniciais do usuário logado no menu e na barra superior.
    document.addEventListener("DOMContentLoaded", () => {
        const nome = localStorage.getItem("nomeUsuario");
        if (!nome) return;
        const iniciais = nome.trim().split(/\s+/).map(p => p[0]).slice(0, 2).join("").toUpperCase();
        setTimeout(() => {
            document.querySelectorAll(".sidebar-user strong").forEach(el => el.textContent = nome);
            document.querySelectorAll(".avatar, .top-avatar").forEach(el => el.textContent = iniciais);
        });
    });
})();
