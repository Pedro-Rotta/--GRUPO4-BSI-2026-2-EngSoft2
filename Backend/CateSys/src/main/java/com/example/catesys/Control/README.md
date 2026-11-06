# Control (Controle)

Pacote: `com.example.catesys.Control`

## O que fica aqui
- **Uma Control por caso de uso**, nomeada pelo caso de uso.
  Ex.: `GerenciarCatequizandoControl`, `MatricularCatequizandoControl`.
- A **classe de conexão com o banco** (`Conexao`), implementada como **Singleton** (GOF).

## Fluxo de cada método
```
1. recebe o request do front (record XxxRequest)
2. instancia a Model e chama model.validar()  → se houver erros, devolve 400
3. pega a Conexao e instancia o DAO (passando a Conexao no construtor)
4. executa a operação passando a Model para o DAO: dao.gravar(model)
5. commit (ou rollback em caso de erro)
6. devolve a response (record XxxResponse)
```

## Como codificar
- É um `@RestController` com `@RequestMapping("/api/<entidade-no-plural>")`.
- Executa **todos os passos do caso de uso** (GRASP Controlador).
- Funciona como **Façade**: o front chama um único endpoint e a Control coordena Model, DAO e Conexao por dentro.
- **Instancia a Model e o DAO** (GRASP Criador: é ela que tem os dados do request).
- Declara o DAO pela interface: `IDAO<Catequizando> dao = new CatequizandoDAO(conexao);` (SOLID-D).
- Contém os **ifs de regra de negócio** que dependem do banco
  (ex.: "só matricula se a turma tiver vaga", "não exclui catequizando com matrícula").
- **Controla a transação**: `commit` no sucesso, `rollback` no erro.
  Nas leituras, `rollback` no `finally` para encerrar a transação aberta.
- Métodos `synchronized`, porque todas as requisições usam a mesma `Conexao`.

### Conexao (Singleton)

```java
public class Conexao {
    private static Conexao instancia;
    private Connection connection;

    private Conexao() throws SQLException { /* lê application.properties e abre a conexão (autoCommit = false) */ }

    public static synchronized Conexao getInstancia() throws SQLException {
        if (instancia == null || instancia.connection.isClosed() || !instancia.connection.isValid(2))
            instancia = new Conexao();
        return instancia;
    }

    public Connection getConnection() { return connection; }
    public void commit() throws SQLException { connection.commit(); }
    public void rollback() throws SQLException { connection.rollback(); }
}
```

### Exemplo de método

```java
@PostMapping
public synchronized ResponseEntity<?> cadastrar(@RequestBody CatequizandoRequest request) throws SQLException {
    Catequizando catequizando = new Catequizando(request.nome(), ...);    // instancia a Model
    List<String> erros = catequizando.validar();                         // valida
    if (!erros.isEmpty())
        return ResponseEntity.badRequest().body(Map.of("erros", erros));

    Conexao conexao = Conexao.getInstancia();
    IDAO<Catequizando> dao = new CatequizandoDAO(conexao);               // instancia o DAO
    try {
        dao.gravar(catequizando);                                        // DAO recebe a Model
        conexao.commit();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensagem", "Catequizando cadastrado com sucesso"));
    } catch (SQLException | RuntimeException e) {
        conexao.rollback();
        throw e;
    }
}
```

## O que NÃO fazer
- Não escrever SQL aqui (o SQL fica no `DAO`).
- Não validar atributos aqui (a validação fica na `Model`, no `validar()`).
- Não deixar a transação aberta: toda escrita termina em `commit` ou `rollback`.
