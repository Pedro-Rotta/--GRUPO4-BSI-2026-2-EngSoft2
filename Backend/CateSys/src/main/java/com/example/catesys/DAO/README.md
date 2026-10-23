# DAO (Data Access Object): persistência

Pacote: `com.example.catesys.DAO`

## O que fica aqui
- **Um DAO por entidade**: `CatequistaDAO`, `TurmaDAO`, `MatriculaDAO` etc.
- A **classe de conexão com o banco** (ex.: `Conexao`), implementada como **Singleton**.

## Como codificar
- O DAO só tem **métodos de persistir e recuperar**: `inserir`, `alterar`, `excluir`,
  `buscarPorId`, `listar`.
- **Sem atributos** de dados: recebe a entidade (ou o id) por parâmetro e devolve entidades.
- Todo o **SQL** do sistema fica aqui.
- Usa a conexão/transação já aberta pela `Control` (pega a instância única da `Conexao`).

```java
public class Conexao {
    private static Conexao instancia;
    private Conexao() { /* abre a conexão */ }
    public static Conexao getInstancia() {
        if (instancia == null)
            instancia = new Conexao();
        return instancia;
    }
}
```

## O que NÃO fazer
- Não colocar validação nem regra de negócio.
- Não fazer commit/rollback por conta própria (quem decide é a `Control`).
- Não ser chamado pela `View` nem pela `Control` direto: o caminho é Control → Model → DAO.
