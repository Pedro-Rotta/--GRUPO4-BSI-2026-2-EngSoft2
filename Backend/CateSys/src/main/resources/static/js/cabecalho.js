// Preenche o menu lateral, o rodapé e a data com os dados da parametrização.
// Os elementos são achados pelas classes do CSS,
// então funciona em qualquer tela que use esse layout, sem mudar o HTML dela
(function () {
    const marca = document.querySelector('.sidebar-brand');
    const icone = marca && marca.querySelector('img');
    const nomeNoMenu = marca && marca.querySelector('span');   // <span>Cate<span>Sys</span></span>
    const rodape = document.querySelector('.app-footer');
    const data = document.querySelector('.today-date');

    // Guarda como a tela veio escrita: é o que aparece enquanto não existe parametrização
    const iconeOriginal = icone ? icone.getAttribute('src') : '';
    const nomeOriginal = nomeNoMenu ? nomeNoMenu.innerHTML : '';
    const rodapeOriginal = rodape ? rodape.textContent : '';

    // Imagem do projeto (img/logo.png) vira /img/logo.png, para funcionar em qualquer pasta
    function caminhoDaImagem(logo) {
        return (/^(https?:)?\/\//.test(logo) || logo.startsWith('/')) ? logo : '/' + logo;
    }

    function mostrarData() {
        if (!data) return;
        data.textContent = new Date().toLocaleDateString('pt-BR',
            { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' });
    }

    function usarDados(d) {
        if (nomeNoMenu) {
            nomeNoMenu.textContent = d.nome;                    // textContent: nunca interpreta HTML
            nomeNoMenu.title = d.nome;
            // nome grande: letra menor e no máximo 2 linhas, para caber no menu
            nomeNoMenu.style.fontSize = d.nome.length > 14 ? '1rem' : '';
            nomeNoMenu.style.lineHeight = '1.15';
            nomeNoMenu.style.minWidth = '0';
            nomeNoMenu.style.overflow = 'hidden';
            nomeNoMenu.style.display = '-webkit-box';
            nomeNoMenu.style.webkitLineClamp = '2';
            nomeNoMenu.style.webkitBoxOrient = 'vertical';
        }
        if (icone) {
            icone.src = d.logotipoPequeno ? caminhoDaImagem(d.logotipoPequeno) : iconeOriginal;
            icone.alt = 'Logotipo de ' + d.nome;
        }
        if (rodape) {
            const local = [d.cidade, d.uf].filter(Boolean).join('/');
            rodape.textContent = ['CateSys', d.nome, local].filter(Boolean).join(' · ');
        }
    }

    function usarPadrao() {
        if (nomeNoMenu) {
            nomeNoMenu.innerHTML = nomeOriginal;
            nomeNoMenu.removeAttribute('style');
            nomeNoMenu.removeAttribute('title');
        }
        if (icone) icone.src = iconeOriginal;
        if (rodape) rodape.textContent = rodapeOriginal;
    }

    // Se a imagem cadastrada não for encontrada, volta para o ícone padrão
    if (icone) {
        icone.addEventListener('error', () => {
            if (icone.getAttribute('src') !== iconeOriginal) icone.src = iconeOriginal;
        });
    }

    async function atualizar() {
        try {
            const resp = await fetch('/api/parametrizacao');
            if (!resp.ok) {                                     // 404: ainda não foi parametrizado
                usarPadrao();
                return;
            }
            usarDados(await resp.json());
        } catch (e) {
            usarPadrao();
        }
    }

    // A tela de parametrização chama isso depois de salvar, para o menu mudar na hora
    window.atualizarCabecalho = atualizar;

    // Em telas baixas o menu rola: deixa o item da página atual visível.
    // Repete quando a página termina de carregar, porque as fontes mudam a altura do menu.
    function mostrarItemAtivo() {
        const item = document.querySelector('.sidebar-nav .nav-link.active');
        if (item) item.scrollIntoView({ block: 'nearest' });
    }
    mostrarItemAtivo();
    window.addEventListener('load', mostrarItemAtivo);

    mostrarData();
    atualizar();
})();