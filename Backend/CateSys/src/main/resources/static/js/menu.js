
document.addEventListener("DOMContentLoaded", () => {

    const sidebar = document.getElementById("sidebar");
    const overlay = document.getElementById("sidebarOverlay");
    const toggle = document.getElementById("menuToggle");

    if (!sidebar || !overlay || !toggle) {
        return;
    }

    const paginas = [
        {
            titulo: "Painel inicial",
            url: "/html/dashboard.html",
            icone: "bi-grid-1x2"
        },
        {
            titulo: "Catequizandos",
            url: "/html/catequizandos.html",
            icone: "bi-people"
        },
        {
            titulo: "Catequistas",
            url: "/html/catequistas.html",
            icone: "bi-person-badge"
        },
        {
            titulo: "Turmas",
            url: "/html/turmas.html",
            icone: "bi-easel"
        },
        {
            titulo: "Salas e locais",
            url: "/html/salas.html",
            icone: "bi-building"
        },
        {
            titulo: "Presença",
            url: "/html/presenca.html",
            icone: "bi-calendar-check"
        },
        {
            titulo: "Justificativas",
            url: "/html/justificativas.html",
            icone: "bi-file-earmark-text"
        },
        {
            titulo: "Relatórios",
            url: "/html/relatorios.html",
            icone: "bi-file-bar-graph"
        },
        {
            titulo: "Sobre o CateSys",
            url: "/html/sobre.html",
            icone: "bi-info-circle"
        }
    ];

    sidebar.replaceChildren();

    const marca = document.createElement("a");
    marca.className = "sidebar-brand";
    marca.href = "/html/dashboard.html";

    const imagem = document.createElement("img");
    imagem.src = "/imgs/2-removebg.png";
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

    paginas.forEach((pagina, indice) => {

        if (indice === 0 || indice === 7) {
            const legenda = document.createElement("div");
            legenda.className = "nav-caption";

            legenda.textContent =
                indice === 0
                    ? "MENU PRINCIPAL"
                    : "CONSULTAS";

            navegacao.appendChild(legenda);
        }

        const link = document.createElement("a");
        link.href = pagina.url;
        link.className = "nav-link";

        if (caminhoAtual === pagina.url) {
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
});
