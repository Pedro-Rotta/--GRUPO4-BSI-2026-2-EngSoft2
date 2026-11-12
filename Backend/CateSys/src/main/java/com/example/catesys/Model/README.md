# Model (Modelo / Entidade)

Pacote: `com.example.catesys.Model`

## O que fica aqui
Uma classe por entidade do banco: `Catequista`, `Catequizando`, `Turma`, `Matricula`, `Local`, `Horario` etc.
Os enums ficam em `Model.Enums`.

## Como codificar
- **Atributos privados** que espelham as colunas da tabela, com **get/set**.
- **Validações dos próprios atributos** num método `validar()` (GRASP Especialista),
  que devolve **todos** os erros de uma vez (lista vazia = válido):

```java
public List<String> validar() {
    List<String> erros = new ArrayList<>();
    if (nome == null || nome.isBlank())
        erros.add("Nome é obrigatório");
    if (dataNascimento == null)
        erros.add("Data de nascimento é obrigatória");
    return erros;
}
```

- **Cálculos sobre os próprios dados** também ficam aqui.
  Ex.: quantidade de vagas restantes em `Turma`.
- **Relacionamentos** são mantidos pela entidade que agrega.
  Ex.: `Turma` mantém a lista de `Matricula`.
- Um método `static reconstituir(int id, ...)` para o DAO montar objetos vindos do banco (com id).
- É **instanciada pela `Control`** e **instancia o DAO** (View → Control → Model → DAO).
- **Métodos de persistência** recebem a `Conexao` da Control, criam o DAO e se passam como parâmetro:

```java
public boolean gravar(Conexao conexao) throws SQLException {
    IDAO<Catequizando> dao = new CatequizandoDAO(conexao); // a Model instancia o DAO
    return dao.gravar(this);                               // e se passa como parâmetro
}

public List<Catequizando> buscar(String filtroNome, String filtroResponsavel, Conexao conexao) throws SQLException {
    CatequizandoDAO dao = new CatequizandoDAO(conexao);    // busca com filtro é só deste DAO
    return dao.buscar(filtroNome, filtroResponsavel);
}
```

- Na Control: `catequizando.gravar(conexao)`; para consultas, `new Catequizando().buscar(..., conexao)`.

## O que NÃO fazer
- Não escrever SQL.
- Não chamar `Conexao.getInstancia()`, `commit` nem `rollback`: a transação é da Control.
- Não lançar exceção nos `set` para validar: o `set` no máximo normaliza (`trim()`, `toUpperCase()`).
- Não colocar regra de caso de uso nem acessar a View.
