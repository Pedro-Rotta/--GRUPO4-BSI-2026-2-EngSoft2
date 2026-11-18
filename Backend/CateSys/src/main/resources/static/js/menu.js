
// Monta o menu lateral de todas as telas (<aside id="sidebar">) e preenche
// o nome e o logotipo da paróquia com os dados da parametrização.

document.addEventListener("DOMContentLoaded", () => {

    const sidebar = document.getElementById("sidebar");
    const overlay = document.getElementById("sidebarOverlay");
    const toggle = document.getElementById("menuToggle");

    if (!sidebar || !overlay || !toggle) {
        return;
    }

    const LOGO_PADRAO = "/imgs/2-removebg.png";

    const paginas = [
        { secao: "MENU PRINCIPAL", titulo: "Painel inicial", url: "/html/dashboard.html", icone: "bi-grid-1x2" },
        { titulo: "Catequizandos", url: "/html/catequizandos.html", icone: "bi-people" },
        { titulo: "Catequistas", url: "/html/catequistas.html", icone: "bi-person-badge" },
        {
            titulo: "Turmas", url: "/html/turmas.html", icone: "bi-easel",
            relacionadas: ["/html/ofertar-turma.html", "/html/editar-turma.html"]
        },
        { titulo: "Salas e locais", url: "/html/salas.html", icone: "bi-building" },
        { titulo: "Presença", url: "/html/presenca.html", icone: "bi-calendar-check" },
        { titulo: "Justificativas", url: "/html/justificativas.html", icone: "bi-file-earmark-text" },
        { secao: "CONSULTAS", titulo: "Relatórios", url: "/html/relatorios.html", icone: "bi-file-bar-graph" },
        { titulo: "Sobre o CateSys", url: "/html/sobre.html", icone: "bi-info-circle" },
        // Só usuário de acesso total (secretaria) usa o sistema por enquanto.
        { secao: "ADMINISTRAÇÃO", titulo: "Parametrização", url: "/html/parametrizacao.html", icone: "bi-gear" }
    ];

    sidebar.replaceChildren();

    const marca = document.createElement("a");
    marca.className = "sidebar-brand";
    marca.href = "/html/dashboard.html";

    const imagem = document.createElement("img");
    imagem.src = LOGO_PADRAO;
    imagem.alt = "CateSys";
    imagem.width = 38;
    imagem.height = 38;

    const nome = document.createElement("span");
    nome.innerHTML = "Cate<span>Sys</span>";

    marca.append(imagem, nome);
    sidebar.appendChild(marca);

    const usuario = document.createElement("div");
    usuario.className = "sidebar-user";
    usuario.innerHTML = `
        <div class="avatar">CS</div>
        <div>
            <strong>Secretaria</strong>
            <small>Secretaria paroquial</small>
        </div>
    `;

    sidebar.appendChild(usuario);

    const navegacao = document.createElement("nav");
    navegacao.className = "sidebar-nav";

    const caminhoAtual = window.location.pathname;

    paginas.forEach(pagina => {

        if (pagina.secao) {
            const legenda = document.createElement("div");
            legenda.className = "nav-caption";
            legenda.textContent = pagina.secao;
            navegacao.appendChild(legenda);
        }

        const link = document.createElement("a");
        link.href = pagina.url;
        link.className = "nav-link";

        if (caminhoAtual === pagina.url || (pagina.relacionadas || []).includes(caminhoAtual)) {
            link.classList.add("active");
            link.setAttribute("aria-current", "page");
        }

        const icone = document.createElement("i");
        icone.className = `bi ${pagina.icone}`;

        const texto = document.createElement("span");
        texto.textContent = pagina.titulo;

        link.append(icone, texto);
        navegacao.appendChild(link);
    });

    sidebar.appendChild(navegacao);

    const rodape = document.createElement("div");
    rodape.className = "sidebar-footer";

    const grupo = document.createElement("span");
    grupo.textContent = "Desenvolvido pelo Grupo 4";

    const sair = document.createElement("a");
    sair.href = "/html/index.html";
    sair.className = "btn-logout";
    sair.textContent = "Sair";

    rodape.append(grupo, sair);
    sidebar.appendChild(rodape);

    function setMenu(aberto) {
        sidebar.classList.toggle("open", aberto);
        overlay.classList.toggle("show", aberto);

        toggle.setAttribute(
            "aria-expanded",
            String(aberto)
        );
    }

    toggle.addEventListener("click", () => {
        setMenu(!sidebar.classList.contains("open"));
    });

    overlay.addEventListener("click", () => {
        setMenu(false);
    });

    document.addEventListener("keydown", evento => {
        if (evento.key === "Escape") {
            setMenu(false);
        }
    });

    window.matchMedia("(min-width: 992px)")
        .addEventListener("change", evento => {
            if (evento.matches) {
                setMenu(false);
            }
        });

    const data = document.querySelector(".today-date");

    if (data) {
        data.textContent = new Date().toLocaleDateString(
            "pt-BR",
            {
                day: "2-digit",
                month: "short",
                year: "numeric"
            }
        );
    }

    // ---------- Dados da paróquia (parametrização) ----------

    const rodapePagina = document.querySelector(".app-footer");
    const rodapeOriginal = rodapePagina ? rodapePagina.textContent.trim() : "";

    // "img/logo.png" vira "/img/logo.png", para funcionar em qualquer pasta
    function caminhoDaImagem(logo) {
        return (/^(https?:)?\/\//.test(logo) || logo.startsWith("/")) ? logo : "/" + logo;
    }

    imagem.addEventListener("error", () => {
        if (imagem.getAttribute("src") !== LOGO_PADRAO) imagem.src = LOGO_PADRAO;
    });

    function usarParametrizacao(d) {
        nome.textContent = d.nome;            // textContent: nunca interpreta HTML
        nome.title = d.nome;
        nome.style.fontSize = d.nome.length > 14 ? "1rem" : "";
        nome.style.lineHeight = "1.15";
        nome.style.overflow = "hidden";
        nome.style.display = "-webkit-box";
        nome.style.webkitLineClamp = "2";
        nome.style.webkitBoxOrient = "vertical";

        imagem.src = d.logotipoPequeno ? caminhoDaImagem(d.logotipoPequeno) : LOGO_PADRAO;
        imagem.alt = "Logotipo de " + d.nome;

        if (rodapePagina) {
            const local = [d.cidade, d.uf].filter(Boolean).join("/");
            rodapePagina.textContent = ["CateSys", d.nome, local].filter(Boolean).join(" · ");
        }
    }

    function usarPadrao() {
        nome.innerHTML = "Cate<span>Sys</span>";
        nome.removeAttribute("style");
        nome.removeAttribute("title");
        imagem.src = LOGO_PADRAO;
        if (rodapePagina) rodapePagina.textContent = rodapeOriginal;
    }

    async function atualizarCabecalho() {
        try {
            const resposta = await fetch("/api/parametrizacao");
            if (!resposta.ok) {           // 404: ainda não foi parametrizado
                usarPadrao();
                return;
            }
            usarParametrizacao(await resposta.json());
        } catch (e) {
            usarPadrao();
        }
    }

    // A tela de parametrização chama isto depois de salvar, para o menu mudar na hora.
    window.atualizarCabecalho = atualizarCabecalho;

    atualizarCabecalho();
});
