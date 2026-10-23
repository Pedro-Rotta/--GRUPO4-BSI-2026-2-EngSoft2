# View (Visão)

Pacote: `com.example.catesys.View`

## O que fica aqui
As telas do sistema: janelas, formulários, botões e menus.
Ex.: `TelaMatricula`, `TelaCadastroTurma`, `TelaPrincipal`.

## Como codificar
- Implementa as **ações dos botões** e mostra os resultados para o usuário.
- Obtém a **Control** do caso de uso pela instância única (`getInstancia()`) e chama os métodos dela.
- Envia para a Control apenas **dados simples** digitados na tela (textos, números, datas, ids)
  e exibe o que a Control devolver por `return` (resultado ou mensagem de erro).

```java
// ação do botão "Matricular"
var control = MatricularCatequizandoControl.getInstancia();
String resultado = control.matricular(idCatequizando, idTurma);
mostrarMensagem(resultado);
```

## O que NÃO fazer
- Não instanciar Models nem manipular listas de entidades.
- Não acessar DAO nem escrever SQL.
- Não colocar regra de negócio (ifs de regra ficam na `Control`, validações na `Model`).

Fluxo correto: **View ⇄ Control ⇄ Model ⇄ DAO**.
