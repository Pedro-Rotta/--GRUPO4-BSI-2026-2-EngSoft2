package com.example.catesys.Control;

import com.example.catesys.Model.Catequista;
import com.example.catesys.Model.Turma;
import com.example.catesys.Model.TurmaCatequista;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// RF_F4 - Trocar catequista da turma.
@RestController
@RequestMapping("/api/turmas")
public class TrocarCatequistaTurmaControl {

    public record TrocaCatequistaRequest(Integer catequistaAtualId, Integer novoCatequistaId) {}

    public record CatequistaVinculadoResponse(int id, String nome) {}

    // Catequistas que hoje estão na turma (para escolher quem sai).
    @GetMapping("/{id}/catequistas")
    public synchronized ResponseEntity<?> catequistasDaTurma(@PathVariable int id) throws SQLException {
        Conexao conexao = Conexao.getInstancia();
        try {
            if (new Turma().buscarPorId(id, conexao) == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erros", List.of("Turma não encontrada")));
            List<CatequistaVinculadoResponse> lista = new ArrayList<>();
            for (TurmaCatequista vinculo : new TurmaCatequista().buscarPorTurma(id, conexao)) {
                Catequista c = new Catequista().buscarPorId(vinculo.getCatequistaId(), conexao);
                if (c != null)
                    lista.add(new CatequistaVinculadoResponse(c.getId(), c.getNome()));
            }
            return ResponseEntity.ok(lista);
        } finally {
            conexao.rollback(); // só leitura: encerra a transação
        }
    }

    @PutMapping("/{id}/catequista")
    public synchronized ResponseEntity<?> trocar(@PathVariable int id, @RequestBody TrocaCatequistaRequest request)
            throws SQLException {
        TurmaCatequista vinculo = new TurmaCatequista(valor(request.catequistaAtualId()), id);
        int novoId = valor(request.novoCatequistaId());
        List<String> erros = new ArrayList<>(vinculo.validar());
        if (novoId <= 0)
            erros.add("Informe o novo catequista");
        else if (novoId == vinculo.getCatequistaId())
            erros.add("O novo catequista deve ser diferente do atual");
        if (!erros.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("erros", erros));

        Conexao conexao = Conexao.getInstancia();
        try {
            Turma turma = new Turma().buscarPorId(id, conexao);
            if (turma == null)
                return erro(conexao, HttpStatus.NOT_FOUND, "Turma não encontrada");
            if (!turma.estaAtiva())
                return erro(conexao, HttpStatus.CONFLICT, "Turma encerrada ou cancelada não pode trocar de catequista");
            if (!vinculo.existe(conexao))
                return erro(conexao, HttpStatus.CONFLICT, "O catequista atual não pertence a esta turma");

            Catequista novo = new Catequista().buscarPorId(novoId, conexao);
            if (novo == null)
                return erro(conexao, HttpStatus.NOT_FOUND, "Novo catequista não encontrado");
            if (!novo.estaAtivo())
                return erro(conexao, HttpStatus.CONFLICT, "O novo catequista não está ativo");
            if (new TurmaCatequista(novoId, id).existe(conexao))
                return erro(conexao, HttpStatus.CONFLICT, "O novo catequista já pertence a esta turma");

            turma.setCatequistaId(novoId); // verifica a agenda do novo catequista no horário/período da turma
            if (turma.existeConflitoCatequista(conexao))
                return erro(conexao, HttpStatus.CONFLICT,
                        "Conflito de horário: " + novo.getNome() + " já tem turma neste dia e horário");

            if (!vinculo.trocarPor(novoId, conexao))
                return erro(conexao, HttpStatus.CONFLICT, "Não foi possível trocar o catequista");
            conexao.commit();
            return ResponseEntity.ok(Map.of("mensagem", "Catequista da turma trocado com sucesso"));
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

    @ExceptionHandler({HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<?> requisicaoInvalida(Exception e) {
        return ResponseEntity.badRequest()
                .body(Map.of("erros", List.of("Requisição inválida: verifique os campos enviados")));
    }

    private ResponseEntity<?> erro(Conexao conexao, HttpStatus status, String mensagem) throws SQLException {
        conexao.rollback();
        return ResponseEntity.status(status).body(Map.of("erros", List.of(mensagem)));
    }

    private static int valor(Integer numero) {
        return numero == null ? 0 : numero;
    }
}
