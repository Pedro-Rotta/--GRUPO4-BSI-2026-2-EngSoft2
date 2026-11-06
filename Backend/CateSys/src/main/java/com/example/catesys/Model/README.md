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
- É **instanciada pela `Control`** e **passada como parâmetro para o DAO** (`dao.gravar(model)`).

## O que NÃO fazer
- Não escrever SQL, abrir conexão nem controlar transação.
- **Não conhecer nem chamar o DAO** (baixo acoplamento: a Model é só a entidade).
- Não lançar exceção nos `set` para validar: o `set` no máximo normaliza (`trim()`, `toUpperCase()`).
- Não colocar regra de caso de uso nem acessar a View.
