package com.example.catesys.Control;

import com.example.catesys.Model.Frequencia;
import com.example.catesys.Model.Turma;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// RF_F9 - Lançar presença: grava o encontro da turma e as faltas, tudo na mesma transação.
@RestController
@RequestMapping("/api/presencas")
public class LancarPresencaControl {

    public record PresencaRequest(LocalDate data, Integer turmaId, List<Integer> matriculasAusentes) {}

    @PostMapping
    public synchronized ResponseEntity<?> lancar(@RequestBody PresencaRequest request) throws SQLException {
        Frequencia frequencia = new Frequencia(
                request.data() == null ? null : java.sql.Date.valueOf(request.data()),
                request.turmaId() == null ? 0 : request.turmaId(),
                request.matriculasAusentes());
        List<String> erros = frequencia.validar();
        if (!erros.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("erros", erros));

        Conexao conexao = Conexao.getInstancia();
        try {
            Turma turma = new Turma().buscarPorId(frequencia.getTurmaId(), conexao);
            if (turma == null)
                return erro(conexao, HttpStatus.NOT_FOUND, "Turma não encontrada");
            if (!turma.estaAtiva())
                return erro(conexao, HttpStatus.CONFLICT, "Turma encerrada ou cancelada");
            if (frequencia.jaLancada(conexao))
                return erro(conexao, HttpStatus.CONFLICT, "A presença desta turma já foi lançada nesta data");
            if (!frequencia.faltasPertencemATurma(frequencia.matriculasAtivasDaTurma(conexao)))
                return erro(conexao, HttpStatus.BAD_REQUEST, "Há falta para matrícula que não está ativa nesta turma");

            frequencia.gravar(conexao);
            conexao.commit();
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "mensagem", "Presença lançada com sucesso",
                    "faltas", frequencia.getFaltas().size()));
        } catch (SQLException | RuntimeException e) {
            conexao.rollback();
            throw e;
        }
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<?> erroBanco(SQLException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erros", List.of("Erro ao acessar o banco de dados")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> requisicaoInvalida(Exception e) {
        return ResponseEntity.badRequest()
                .body(Map.of("erros", List.of("Requisição inválida: verifique os campos enviados")));
    }

    private ResponseEntity<?> erro(Conexao conexao, HttpStatus status, String mensagem) throws SQLException {
        conexao.rollback();
        return ResponseEntity.status(status).body(Map.of("erros", List.of(mensagem)));
    }
}
