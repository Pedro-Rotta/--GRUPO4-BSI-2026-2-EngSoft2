# DAO (Data Access Object): persistência

Pacote: `com.example.catesys.DAO`

## O que fica aqui
- **Um DAO por entidade**: `CatequizandoDAO`, `CatequistaDAO`, `TurmaDAO` etc.
- A interface genérica `IDAO<T>`.

## Como codificar
- O DAO só tem **métodos de persistir e recuperar**: `gravar`, `alterar`, `excluir`,
  `buscarPorId`, `buscarTodos`, mais as buscas com filtro específicas da entidade.
- É **instanciado pela `Control`**, que entrega a `Conexao` no construtor com a transação já aberta.
- **Recebe a Model (ou o id) por parâmetro** e devolve Models: `dao.gravar(catequizando)`.
- Monta as Models lidas do banco com o `reconstituir(...)` da entidade.
- Todo o **SQL** do sistema fica aqui, sempre com `PreparedStatement` e `?`.
- Data nula vai com `ps.setNull(posicao, Types.DATE)`.
- As colunas de chave seguem o padrão `<tabela>_id` (ex.: `catequizando_id`, e não `id`).

### IDAO<T>
```java
public interface IDAO<T> {
    boolean gravar(T obj) throws SQLException;
    boolean alterar(T obj) throws SQLException;
    boolean excluir(int id) throws SQLException;
    T buscarPorId(int id) throws SQLException;
    List<T> buscarTodos() throws SQLException;
}
```
Entidade que não tem consulta ou exclusão (ex.: `Parametrizacao`) **não** implementa `IDAO`,
para não ficar com métodos vazios (SOLID: Segregação de Interface).

### Exemplo
```java
public class CatequizandoDAO implements IDAO<Catequizando> {
    private final Conexao conexao;

    public CatequizandoDAO(Conexao conexao) {
        this.conexao = conexao;
    }

    @Override
    public boolean gravar(Catequizando catequizando) throws SQLException {
        String sql = """
                INSERT INTO catequizando (nome, data_nascimento,
                nome_responsavel, telefone_responsavel) VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setString(1, catequizando.getNome());
            definirData(ps, 2, catequizando.getDataNascimento());
            ps.setString(3, catequizando.getNomeResponsavel());
            ps.setString(4, catequizando.getTelefoneResponsavel());
            return ps.executeUpdate() > 0;
        }
    }
}
```

## O que NÃO fazer
- Não colocar validação nem regra de negócio.
- Não fazer `commit`/`rollback` (quem decide é a `Control`).
- Não buscar a conexão sozinho (`Conexao.getInstancia()` dentro do DAO): usa a que a `Control` entregou.
- Não concatenar valores dentro do SQL.
