# View (Visão)

As telas do sistema são **web** (HTML, CSS e JavaScript) e ficam em `src/main/resources/static`,
que é a pasta que o Spring Boot entrega ao navegador. A View não tem classes Java.
Esta pasta `VIEW` guarda só esta documentação.

Ex.: `static/VIEW/catequizandos.html` → `http://localhost:8080/VIEW/catequizandos.html`.

## O que fica na View
- Formulários de cadastro e telas de consulta de cada caso de uso.
- Menus e barra de navegação (logotipo e dados da paróquia vindos da parametrização).

## Como codificar
- Chama a **Control** pela API REST (`fetch` para `/api/...`) e mostra o que ela devolver.
- Envia apenas **dados simples** em JSON (textos, números, datas `yyyy-MM-dd`, ids).
- **Máscaras** nos campos que precisam: data, telefone, CPF, CNPJ, CEP.
- **Destaca e valida os obrigatórios** antes de enviar (Bootstrap form validation).
- Mostra mensagens de sucesso/erro **na própria tela** (área de notificação), lendo
  `{"mensagem": "..."}` ou `{"erros": [...]}` da resposta. **Sem `alert()`**;
  modal só para erro fatal de conexão.
- Telas de consulta com **pelo menos 2 filtros (nunca por código/PK)** e opção de carregar o registro para alteração.

```javascript
// ação do botão "Salvar"
const resp = await fetch('/api/catequizandos', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dados)
});
const corpo = await resp.json();
mostrarMensagem(resp.ok ? corpo.mensagem : corpo.erros.join('<br>'));
```

## O que NÃO fazer
- Não colocar regra de negócio (ifs de regra ficam na `Control`).
- Não confiar só na validação da tela: ela é conveniência para o usuário; a `Model` valida de novo.
- Não acessar banco, DAO ou Model diretamente: tudo passa pela API da `Control`.

Fluxo: **View → (requisita) Control → (instancia) Model → (instancia) DAO**.
A View só conhece a API; a Control instancia a Model, e a Model instancia o DAO.

## Como rodar
Precisa do **JDK 17**. Na pasta `Backend/CateSys`:

```bash
./gradlew bootRun
```

Depois abra no navegador, por exemplo `http://localhost:8080/VIEW/catequizandos.html`.
Para parar, `Ctrl+C` no terminal.
