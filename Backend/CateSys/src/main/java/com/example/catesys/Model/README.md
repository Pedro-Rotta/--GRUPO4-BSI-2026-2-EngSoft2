# Model (Modelo / Entidade)

Pacote: `com.example.catesys.Model`

## O que fica aqui
Uma classe por entidade do banco: `Catequista`, `Catequizando`, `Turma`, `Matricula`, `Local`, `Horario` etc.

## Como codificar
- **Atributos privados** que espelham as colunas da tabela, com **get/set**.
- **Validações dos próprios atributos** ficam na entidade (GRASP Especialista).
  Ex.: validar CPF ou data de nascimento em `Catequizando`, idade mínima em `Matricula`.
- **Cálculos sobre os próprios dados** também ficam aqui.
  Ex.: quantidade de vagas restantes em `Turma`.
- **Relacionamentos** são mantidos pela entidade que agrega.
  Ex.: `Turma` mantém a lista de `Matricula`.
- A Model chama o **DAO** correspondente para persistir/recuperar (ex.: `Turma` usa `TurmaDAO`).

## O que NÃO fazer
- Não escrever SQL aqui (o SQL fica no `DAO`).
- Não abrir conexão nem controlar transação (isso é da `Control`).
- Não colocar regra de caso de uso nem acessar a `View`.
