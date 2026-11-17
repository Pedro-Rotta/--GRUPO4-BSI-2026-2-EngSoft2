package com.example.catesys.Control;

import com.example.catesys.Model.Catequista;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

// RF_B3 - Gerenciar Catequista (inserir, alterar, excluir e consultar).
@RestController
@RequestMapping("/api/catequistas")
public class GerenciarCatequistaControl {

    public record CatequistaRequest(String nome, String cpf, String telefone, String email,
                                    Integer secretariaId, LocalDate dataInicio, LocalDate dataFim) {}

    public record CatequistaResponse(int id, String nome, String cpf, String telefone, String email,
                                     Integer secretariaId, LocalDate dataInicio, LocalDate dataFim) {}

    @PostMapping
    public synchronized ResponseEntity<?> cadastrar(@RequestBody CatequistaRequest request) throws SQLException {
        Catequista catequista = new Catequista(request.nome(), request.cpf(), request.telefone(), request.email(),
                request.secretariaId(), converter(request.dataInicio()), converter(request.dataFim()));
        List<String> erros = catequista.validar();
        if (!erros.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("erros", erros));

        Conexao conexao = Conexao.getInstancia();
        try {
            if (catequista.cpfDuplicado(conexao))
                return erro(conexao, HttpStatus.CONFLICT, "Já existe um catequista cadastrado com esse CPF");
            catequista.gravar(conexao);                  // a Model instancia o DAO
            conexao.commit();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensagem", "Catequista cadastrado com sucesso"));
        } catch (SQLException | RuntimeException e) {
            conexao.rollback();
            throw e;
        }
    }

    @PutMapping("/{id}")
    public synchronized ResponseEntity<?> alterar(@PathVariable int id, @RequestBody CatequistaRequest request)
            throws SQLException {
        Catequista catequista = new Catequista(id, request.nome(), request.cpf(), request.telefone(),
                request.email(), request.secretariaId(), converter(request.dataInicio()), converter(request.dataFim()));
        List<String> erros = catequista.validar();
        if (!erros.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("erros", erros));

        Conexao conexao = Conexao.getInstancia();
        try {
            if (catequista.cpfDuplicado(conexao))
                return erro(conexao, HttpStatus.CONFLICT, "Já existe um catequista cadastrado com esse CPF");
            if (!catequista.alterar(conexao))
                return erro(conexao, HttpStatus.NOT_FOUND, "Catequista não encontrado");
            conexao.commit();
            return ResponseEntity.ok(Map.of("mensagem", "Catequista alterado com sucesso"));
        } catch (SQLException | RuntimeException e) {
            conexao.rollback();
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public synchronized ResponseEntity<?> excluir(@PathVariable int id) throws SQLException {
        Catequista catequista = new Catequista(id);
        Conexao conexao = Conexao.getInstancia();
        try {
            if (catequista.possuiTurma(conexao))
                return erro(conexao, HttpStatus.CONFLICT, "Catequista possui turma e não pode ser excluído");
            if (!catequista.excluir(conexao))
                return erro(conexao, HttpStatus.NOT_FOUND, "Catequista não encontrado");
            conexao.commit();
            return ResponseEntity.ok(Map.of("mensagem", "Catequista excluído com sucesso"));
        } catch (SQLException | RuntimeException e) {
            conexao.rollback();
            throw e;
        }
    }

    @GetMapping("/{id}")
    public synchronized ResponseEntity<?> buscarPorId(@PathVariable int id) throws SQLException {
        Conexao conexao = Conexao.getInstancia();
        try {
            Catequista catequista = new Catequista().buscarPorId(id, conexao);
            if (catequista == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erros", List.of("Catequista não encontrado")));
            return ResponseEntity.ok(converter(catequista));
        } finally {
            conexao.rollback(); // só leitura: encerra a transação
        }
    }

    @GetMapping
    public synchronized List<CatequistaResponse> buscar(@RequestParam(required = false) String nome,
                                                        @RequestParam(required = false) String cpf)
            throws SQLException {
        Conexao conexao = Conexao.getInstancia();
        try {
            return new Catequista().buscar(nome, cpf, conexao).stream().map(this::converter).toList();
        } finally {
            conexao.rollback();
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

    private Date converter(LocalDate data) {
        return data == null ? null : java.sql.Date.valueOf(data);
    }

    private static LocalDate paraLocalDate(Date data) {
        return data == null ? null : new java.sql.Date(data.getTime()).toLocalDate();
    }

    private CatequistaResponse converter(Catequista c) {
        return new CatequistaResponse(c.getId(), c.getNome(), c.getCpf(), c.getTelefone(), c.getEmail(),
                c.getSecretariaId(), paraLocalDate(c.getDataInicio()), paraLocalDate(c.getDataFim()));
    }
}
