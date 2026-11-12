# CLAUDE.md — CateSys

Instruções para assistentes de IA que trabalham neste repositório.
Trabalho acadêmico de Engenharia de Software II (Unoeste/FIPP, 2º/2026, Grupo 4).
Sistema: **CateSys – Sistema de Gerenciamento Catequético** da Paróquia São Francisco Xavier (Bastos/SP).

Documentos de referência na raiz: `TrabEngGrupo4formatadoFinal (1).pdf` (Manual/ERS) e
`Instrucoes para implementacao (1).docx` (requisitos de implementação da professora).

## Regras de trabalho (obrigatórias)

- **Não criar arquivos ou pastas sem perguntar.** Mostrar código no chat é permitido.
- **Não fazer commit/push sem o usuário pedir.** Nunca adicionar `Co-Authored-By: Claude`.
- Mexer **somente** nos arquivos que o usuário pediu.
- Não commitar `~$*.docx` (arquivo temporário do Word) nem `.idea/workspace.xml`.
- Responder em português.

## Stack

- Java 17, Spring Boot 4 (`spring-boot-starter-webmvc`), Gradle (`Backend/CateSys`).
- PostgreSQL hospedado no **Neon** (nuvem), acessado por **JDBC puro** (driver `org.postgresql:postgresql`).
  Não usar JPA/Hibernate nem `JdbcTemplate`: a transação e o Singleton precisam ficar visíveis no código.
- Credenciais em `src/main/resources/application.properties` (`db.url`, `db.user`, `db.password`).
- Front: HTML/CSS/JS em `src/main/resources/static`, consumindo a API REST (`/api/...`).

## Requisitos de arquitetura (docx da professora)

Usar **MVC** (View, Control, Model), os **5 padrões GRASP**, princípios **SOLID** e os **GOF Singleton e Façade**.

## Arquitetura: MVC + DAO (View → Control → Model → DAO)

Pacote base: `com.example.catesys`.

| Pacote    | Responsabilidade                                                                  |
|-----------|-----------------------------------------------------------------------------------|
| View      | Front em `resources/static` (HTML/CSS/JS). Chama a API e mostra o resultado.      |
| `Control` | Um controlador por caso de uso (`@RestController`) + a classe `Conexao` Singleton. Instancia a Model. |
| `Model`   | Entidades (uma por tabela): dados, `validar()`, cálculos próprios. Instancia o DAO. |
| `DAO`     | Todo o SQL. Um DAO por entidade, implementando `IDAO<T>`.                         |

### Fluxo obrigatório (exigido pela professora)

```
View ──(requisita)──▶ Control ──(instancia)──▶ Model ──(instancia)──▶ DAO

Control
  1. recebe o request (record XxxRequest)
  2. instancia a Model  → model.validar()  → se houver erros: 400
  3. pega a Conexao (Singleton)
  4. chama a Model: model.gravar(conexao) / model.alterar(conexao) / model.buscar...(..., conexao)
       Model → instancia o DAO (new XxxDAO(conexao)) e chama dao.gravar(this)
  5. commit (ou rollback em erro)
  6. retorna a response (record XxxResponse)
```

- **A Control instancia a Model** e **nunca** o DAO (não importa o pacote `DAO`).
- **A Model instancia o DAO** e se passa como parâmetro (`dao.gravar(this)`).
- A `Conexao` é obtida pela Control e repassada à Model, para a transação ficar na Control.
- A View nunca acessa Model, DAO ou Conexao.

## Regras por camada

### Control (`com.example.catesys.Control`)
FAZ:
- Uma classe por caso de uso, nomeada pelo caso de uso: `GerenciarCatequizandoControl`, `MatricularCatequizandoControl`.
- `@RestController` + `@RequestMapping("/api/<entidade-no-plural>")`.
- Executa todos os passos do caso de uso (GRASP Controlador) e é a **Façade** para o front.
- Converte request → Model e Model → response (records internos `XxxRequest` / `XxxResponse`).
- `if`s de regra de negócio que dependem do banco (ex.: CPF duplicado, turma sem vaga, não excluir com matrícula ativa).
- Controla a transação: `commit` no sucesso, `rollback` no erro; nas leituras, `rollback` no `finally` para encerrar a transação.
- Métodos `synchronized`, porque todas as requisições compartilham a mesma `Conexao`.
- Chama os métodos de persistência da Model passando a `Conexao`: `catequizando.gravar(conexao)`.
- Para consultas, instancia uma Model vazia: `new Catequizando().buscar(nome, responsavel, conexao)`.

NÃO FAZ:
- SQL.
- Instanciar ou chamar DAO.
- Validação de atributos (é da Model).

### Model (`com.example.catesys.Model`)
FAZ:
- Atributos privados espelhando as colunas, get/set.
- `public List<String> validar()` que devolve **todos** os erros (lista vazia = válido). GRASP Especialista.
- Cálculos sobre os próprios dados (ex.: vagas restantes em `Turma`).
- `static Xxx reconstituir(int id, ...)` para montar objetos vindos do banco (com id).
- Métodos de persistência que recebem a `Conexao` da Control, **instanciam o DAO** e se passam como parâmetro.
  Declarar o DAO pela interface quando possível: `IDAO<Catequizando> dao = new CatequizandoDAO(conexao);` (SOLID-D);
  buscas específicas (ex.: com filtros) usam o tipo concreto.

NÃO FAZ:
- SQL.
- `Conexao.getInstancia()`, `commit` ou `rollback` (a transação é da Control).
- Lançar exceção em `set` para validar (o `set` no máximo normaliza: `trim()`, `toUpperCase()`).

### DAO (`com.example.catesys.DAO`)
FAZ:
- Implementa `IDAO<T>`; buscas com filtro específicas ficam só no DAO da entidade.
- Recebe a `Conexao` no construtor e usa `conexao.getConnection().prepareStatement(sql)`.
- É instanciado pela Model; recebe a Model (ou o id) por parâmetro e devolve Models (usando `reconstituir`).
- Usa sempre `PreparedStatement` com `?` (nunca concatenar valores no SQL).
- Trata datas nulas com `ps.setNull(pos, Types.DATE)`.

NÃO FAZ:
- Validação, regra de negócio, `commit`/`rollback`.
- `Conexao.getInstancia()` dentro do DAO.

### View (`resources/static`)
FAZ:
- Máscaras (data, telefone, CPF, CNPJ, CEP), destaque e validação de obrigatórios (Bootstrap form validation).
- Mensagens na própria tela (área de notificação), **sem `alert()`**. Modal só para erro fatal de conexão.
- Chama a API e exibe a mensagem/lista retornada.

NÃO FAZ:
- Regra de negócio. A validação do front é só conveniência; a Model valida de novo.

## Padrões de código

### `IDAO<T>`
```java
public interface IDAO<T> {
    boolean gravar(T obj) throws SQLException;
    boolean alterar(T obj) throws SQLException;
    boolean excluir(int id) throws SQLException;
    T buscarPorId(int id) throws SQLException;
    List<T> buscarTodos() throws SQLException;
}
```
Entidade sem consulta/exclusão (ex.: `Parametrizacao`) **não** implementa `IDAO`: tem só os métodos que usa (SOLID-I).

### `Conexao` (Singleton GOF, pacote `Control`)
```java
public static synchronized Conexao getInstancia() throws SQLException {
    if (instancia == null || instancia.connection.isClosed() || !instancia.connection.isValid(2))
        instancia = new Conexao();   // reabre se o Neon derrubou a conexão ociosa
    return instancia;
}
public Connection getConnection() { ... }
public void commit() throws SQLException { ... }
public void rollback() throws SQLException { ... }
```
`autoCommit` é `false`: toda escrita precisa de `commit()` explícito.

### Método de persistência na Model
```java
public boolean gravar(Conexao conexao) throws SQLException {
    IDAO<Catequizando> dao = new CatequizandoDAO(conexao);                  // a Model instancia o DAO
    return dao.gravar(this);                                                // e se passa como parâmetro
}

public Catequizando buscarPorId(int id, Conexao conexao) throws SQLException {
    IDAO<Catequizando> dao = new CatequizandoDAO(conexao);
    return dao.buscarPorId(id);
}
```

### Método de escrita na Control
```java
@PostMapping
public synchronized ResponseEntity<?> cadastrar(@RequestBody CatequizandoRequest request) throws SQLException {
    Catequizando catequizando = new Catequizando(request.nome(), ...);       // instancia a Model
    List<String> erros = catequizando.validar();                            // valida
    if (!erros.isEmpty())
        return ResponseEntity.badRequest().body(Map.of("erros", erros));

    Conexao conexao = Conexao.getInstancia();
    try {
        catequizando.gravar(conexao);                                       // a Model instancia o DAO
        conexao.commit();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensagem", "Catequizando cadastrado com sucesso"));
    } catch (SQLException | RuntimeException e) {
        conexao.rollback();
        throw e;
    }
}
```

### Método de leitura na Control
```java
@GetMapping("/{id}")
public synchronized ResponseEntity<?> buscarPorId(@PathVariable int id) throws SQLException {
    Conexao conexao = Conexao.getInstancia();
    try {
        Catequizando c = new Catequizando().buscarPorId(id, conexao);
        if (c == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erros", List.of("Catequizando não encontrado")));
        return ResponseEntity.ok(converter(c));
    } finally {
        conexao.rollback();   // só leitura: encerra a transação
    }
}
```

### Contrato da API
| Ação          | Método e rota              | Sucesso                      | Erros                         |
|---------------|----------------------------|------------------------------|-------------------------------|
| Cadastrar     | `POST /api/xxx`            | 201 `{"mensagem": "..."}`    | 400 `{"erros": [...]}`        |
| Alterar       | `PUT /api/xxx/{id}`        | 200 `{"mensagem": "..."}`    | 400 / 404 `{"erros": [...]}`  |
| Excluir       | `DELETE /api/xxx/{id}`     | 200 `{"mensagem": "..."}`    | 404 / 409 `{"erros": [...]}`  |
| Buscar por id | `GET /api/xxx/{id}`        | 200 objeto                   | 404                           |
| Listar/filtrar| `GET /api/xxx?filtro=...`  | 200 lista                    |                               |
Datas trafegam como `"yyyy-MM-dd"` (`LocalDate` nos records).

## Mapa GRASP / SOLID / GOF (para conferir cada caso de uso)

- **Especialista:** Model valida os próprios dados e sabe se persistir (via DAO); DAO conhece o SQL.
- **Criador:** a Control tem os dados do request, então cria a Model; a Model usa o DAO, então cria o DAO.
- **Controlador:** uma Control por caso de uso.
- **Baixo Acoplamento:** Control não conhece DAO; Model depende de `IDAO<T>`; View só conhece a API.
- **Alta Coesão:** cada classe com um papel só.
- **SOLID:** S (uma responsabilidade por classe), O (novos DAOs sem alterar os existentes), L (DAOs substituíveis via `IDAO`, sem métodos vazios), I (interfaces só com o que a entidade usa), D (Model depende de `IDAO<T>`).
- **Singleton:** `Conexao`. **Façade:** cada Control é o ponto único de entrada do caso de uso.

## Banco de dados (PostgreSQL no Neon, banco `CateSys`)

Chave primária de cada tabela: `<tabela>_id` (ex.: `catequizando_id`, **não** `id`), gerada por `IDENTITY`.

| Tabela              | Colunas principais                                                                                     |
|---------------------|--------------------------------------------------------------------------------------------------------|
| `secretaria`        | `secretaria_id`, nome, cpf (único), email (único), senha, telefone, data_admissao, data_demissao        |
| `catequista`        | `catequista_id`, nome, cpf (único), telefone, email, `secretaria_id`→secretaria, data_inicio, data_fim, senha |
| `catequizando`      | `catequizando_id`, nome, data_nascimento, nome_responsavel, telefone_responsavel (obrigatórios), documento, email, endereco, bairro, cidade, uf, cep, email_resp, telefone, sexo |
| `sala`              | `local_id`, nome, capacidade                                                                           |
| `horario`           | `horario_id`, dia_semana, hora_inicio, hora_fim                                                         |
| `turma`             | `turma_id`, etapa, ano_oferta, data_inicio, data_fim, vagas_totais, vagas_disponiveis, status (default 'Ofertada'), local_horario_id, `sala_local_id`→sala, `horario_horario_id`→horario |
| `matricula`         | `matricula_id`, data_matricula, status (default 'Ativa'), `catequizando_id`, `turma_id`, `secretaria_id`, motivo_desistencia, data_desistencia |
| `turma_catequista`  | (`catequista_catequista_id`, `turma_turma_id`)                                                         |
| `frequencia`        | (`data`, `turma_turma_id`)                                                                             |
| `frequencia_faltas` | (`frequencia_data`, `frequencia_turma_turma_id`, `matricula_matricula_id`), justificativa_bonificacao  |
| `parametrizacao`    | `parametrizacao_id` (sem IDENTITY, sempre 1), nome, cnpj, responsavel, contatos, endereço, logotipo_grande, logotipo_pequeno |

Atenção:
- `matricula` → `catequizando`/`turma` é `ON DELETE CASCADE`: excluir catequizando apaga as matrículas. A Control deve **impedir** a exclusão se houver matrícula.
- Valores padrão de status (`'Ofertada'`, `'Ativa'`) diferem dos enums Java (`StatusTurma`, `StatusMatricula`). Alinhar antes de gravar enums.
- O PDF (seção 4.2) tem `local` e `local_horario`; o banco atual tem `sala` e não tem `local_horario`.

## Requisitos funcionais da implementação (docx)

**Funções básicas (CRUD)** — RF_B1 Catequizando, RF_B2 Secretaria, RF_B3 Catequista, RF_B4 Sala, RF_B5 Turma:
- Inserir, alterar, excluir e consultar.
- Tela de consulta com **pelo menos 2 filtros, nenhum por código/PK**, e carregamento do registro para alteração.
- Máscaras e validação de obrigatórios nas telas.

**Funções fundamentais** — RF_F1 Matricular, RF_F2 Desmatricular, RF_F3 Trocar turma do catequizando,
RF_F4 Trocar catequista da turma, RF_F5 Ofertar turma, RF_F8 Atribuir local à turma, RF_F9 Lançar presença,
RF_F10 Lançar justificativa de falta. Todas com transação (rollback em falha). Fluxos detalhados no PDF, seção 3.2.

**Controle de acesso:**
- Política de primeiro acesso (ex.: usuário ADM inicial de uso único).
- Pelo menos 2 níveis: acesso total e restrito (catequista só lança presença das suas turmas).
- Desativar usuário sem excluir; login valida se está ativo.
- Não permitir dois usuários com o mesmo login.
- Não permitir excluir/desativar todos os usuários de acesso total.

**Parametrização (dados da paróquia):**
- Registro único (`parametrizacao_id = 1`): a tela insere se vazia, altera se existir. Sem consulta nem exclusão.
- A cada acesso, se não houver parametrização, abrir a tela de parametrização automaticamente.
- Opção no menu só para usuário de acesso total.
- Logotipos e dados da paróquia nas telas sempre lidos dessa tabela.

## Testes

- Compilar: `cd Backend/CateSys && ./gradlew compileJava` (JDK 17).
- Rodar: `./gradlew bootRun` → `http://localhost:8080`.
- Testes de escrita no banco devem limpar o que criaram (ou rodar em transação com `rollback`).
