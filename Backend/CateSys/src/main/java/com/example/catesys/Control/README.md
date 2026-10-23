# Control (Controle)

Pacote: `com.example.catesys.Control`

## O que fica aqui
**Uma Control por caso de uso**, nomeada pelo caso de uso.
Ex.: `MatricularCatequizandoControl`, `CadastrarTurmaControl`.

## Como codificar
- Executa **todos os passos do caso de uso** (GRASP Controlador).
- Recebe os dados da `View`, **instancia e usa as Models** e devolve o resultado por `return`.
- Funciona como **Façade**: a View chama um único método e a Control coordena Models e DAOs por dentro.
- Contém os **ifs de regra de negócio** (ex.: "só matricula se a turma tiver vaga")
  e os **loops de processamento** (ex.: gravar cada matrícula de uma lista).
- **Abre a conexão, inicia a transação** e decide quando validar, persistir, fazer commit ou rollback.
- É um **Singleton**, para não perder a conexão e a transação:

```java
public class MatricularCatequizandoControl {
    private static MatricularCatequizandoControl instancia;

    private MatricularCatequizandoControl() { }

    public static MatricularCatequizandoControl getInstancia() {
        if (instancia == null)
            instancia = new MatricularCatequizandoControl();
        return instancia;
    }
}
```

## O que NÃO fazer
- Não escrever SQL aqui.
- Não montar tela nem mostrar mensagens (isso é da `View`).
- Não chamar o DAO direto pulando a Model.
