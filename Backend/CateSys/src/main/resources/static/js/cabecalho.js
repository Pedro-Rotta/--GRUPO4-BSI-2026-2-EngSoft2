// Cabeçalho do sistema: mostra o logotipo e o nome da catequese lidos da parametrização.
// Para usar numa tela, inclua no fim do <body> (a tela precisa carregar o Bootstrap):
//   <script src="js/cabecalho.js"></script>                       (barra com o logotipo pequeno)
//   <script src="js/cabecalho.js" data-logo="grande"></script>    (faixa com o logotipo grande)
(function () {
    const NOME_PADRAO = 'CateSys';   // usado enquanto a parametrização não existe
    const grande = document.currentScript && document.currentScript.dataset.logo === 'grande';

    // Imagem do projeto (img/logo.png) vira /img/logo.png, para funcionar em qualquer tela
    function caminhoDaImagem(logo) {
        return (/^(https?:)?\/\//.test(logo) || logo.startsWith('/')) ? logo : '/' + logo;
    }

    function montar(nome, logo) {
        let cab = document.getElementById('cabecalho-sistema');
        if (!cab) {
            cab = document.createElement('header');
            cab.id = 'cabecalho-sistema';
            document.body.prepend(cab);
        }
        cab.textContent = '';
        cab.className = grande
            ? 'bg-white border-bottom py-4 text-center'
            : 'bg-white border-bottom py-2 px-3 d-flex align-items-center gap-3';

        if (logo) {
            const img = document.createElement('img');
            img.src = caminhoDaImagem(logo);
            img.alt = 'Logotipo de ' + nome;
            img.style.maxHeight = grande ? '120px' : '40px';
            img.addEventListener('error', () => img.remove());   // imagem não encontrada: mostra só o nome
            cab.appendChild(img);
        }

        const titulo = document.createElement(grande ? 'h1' : 'span');
        titulo.className = grande ? 'h3 text-primary mt-2 mb-0' : 'fw-semibold';
        titulo.textContent = nome;                                // textContent: nunca interpreta HTML
        cab.appendChild(titulo);
    }

    async function atualizar() {
        try {
            const resp = await fetch('/api/parametrizacao');
            if (!resp.ok) {                                       // 404: ainda não foi parametrizado
                montar(NOME_PADRAO, '');
                return;
            }
            const dados = await resp.json();
            montar(dados.nome || NOME_PADRAO, grande ? dados.logotipoGrande : dados.logotipoPequeno);
        } catch (e) {
            montar(NOME_PADRAO, '');
        }
    }

    // Outras telas podem chamar isto depois de salvar, para o cabeçalho mudar na hora
    window.atualizarCabecalho = atualizar;

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', atualizar);
    } else {
        atualizar();
    }
})();