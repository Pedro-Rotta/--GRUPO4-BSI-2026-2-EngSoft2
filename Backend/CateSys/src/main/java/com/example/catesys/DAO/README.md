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


## Modelo:
@Override
public boolean gravar(Catequizando c) throws SQLException {
String sql = "INSERT INTO catequizando (nome, data_nascimento, nome_responsavel, telefone_responsavel) VALUES (?, ?, ?, ?)";
try (var ps = Conexao.getInstancia().getConnection().prepareStatement(sql)) {
ps.setString(1, c.getNome());
ps.setDate(2, new java.sql.Date(c.getDataNascimento().getTime()));
ps.setString(3, c.getNomeResponsavel());
ps.setString(4, c.getTelefoneResponsavel());
return ps.executeUpdate() > 0;
}
}
@Override
public boolean alterar(Catequizando obj) throws SQLException{
return true;
}
@Override
public boolean excluir(int id) throws SQLException{
return true;
}
@Override
public Catequizando buscarPorId(int id) throws SQLException{
return new Catequizando();
}
@Override
public List<Catequizando> buscarTodos() throws SQLException{
return new ArrayList<Catequizando>();
}