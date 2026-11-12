package com.example.catesys.Control;

import com.example.catesys.Model.Catequizando;
import com.example.catesys.Model.Enums.Sexo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catequizandos")
public class GerenciarCatequizandoControl {

    public record CatequizandoRequest(String nome, LocalDate dataNascimento,
                                      String nomeResponsavel, String telefoneResponsavel,
                                      String documento, String email, String endereco,
                                      String bairro, String cidade, String uf, String cep,
                                      String emailResponsavel, String telefone, String sexo) {}

    public record CatequizandoResponse(int id, String nome, LocalDate dataNascimento,
                                       String nomeResponsavel, String telefoneResponsavel,
                                       String documento, String email, String endereco,
                                       String bairro, String cidade, String uf, String cep,
                                       String emailResponsavel, String telefone, String sexo) {}

    @PostMapping
    public synchronized ResponseEntity<?> cadastrar(@RequestBody CatequizandoRequest request) throws SQLException {
        Catequizando catequizando = new Catequizando(request.nome(), converter(request.dataNascimento()),
                request.nomeResponsavel(), request.telefoneResponsavel(), request.documento());
        preencherOpcionais(catequizando, request);
        List<String> erros = catequizando.validar();
        if (!erros.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("erros", erros));

        Conexao conexao = Conexao.getInstancia();
        try {
            catequizando.gravar(conexao);                // a Model instancia o DAO
            conexao.commit();
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensagem", "Catequizando cadastrado com sucesso"));
        } catch (SQLException | RuntimeException e) {
            conexao.rollback();
            throw e;
        }
    }

    @PutMapping("/{id}")
    public synchronized ResponseEntity<?> alterar(@PathVariable int id, @RequestBody CatequizandoRequest request) throws SQLException {
        Catequizando catequizando = Catequizando.reconstituir(id, request.nome(), converter(request.dataNascimento()),
                request.nomeResponsavel(), request.telefoneResponsavel());
        catequizando.setDocumento(request.documento());
        preencherOpcionais(catequizando, request);
        List<String> erros = catequizando.validar();
        if (!erros.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("erros", erros));

        Conexao conexao = Conexao.getInstancia();
        try {
            if (!catequizando.alterar(conexao)) {
                conexao.rollback();
                return naoEncontrado();
            }
            conexao.commit();
            return ResponseEntity.ok(Map.of("mensagem", "Catequizando alterado com sucesso"));
        } catch (SQLException | RuntimeException e) {
            conexao.rollback();
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public synchronized ResponseEntity<?> excluir(@PathVariable int id) throws SQLException {
        Catequizando catequizando = new Catequizando(id);
        Conexao conexao = Conexao.getInstancia();
        try {
            if (catequizando.possuiMatricula(conexao)) {
                conexao.rollback();
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erros",
                        List.of("Catequizando possui matrícula e não pode ser excluído")));
            }
            if (!catequizando.excluir(conexao)) {
                conexao.rollback();
                return naoEncontrado();
            }
            conexao.commit();
            return ResponseEntity.ok(Map.of("mensagem", "Catequizando excluído com sucesso"));
        } catch (SQLException | RuntimeException e) {
            conexao.rollback();
            throw e;
        }
    }

    @GetMapping("/{id}")
    public synchronized ResponseEntity<?> buscarPorId(@PathVariable int id) throws SQLException {
        Conexao conexao = Conexao.getInstancia();
        try {
            Catequizando catequizando = new Catequizando().buscarPorId(id, conexao);
            if (catequizando == null)
                return naoEncontrado();
            return ResponseEntity.ok(converter(catequizando));
        } finally {
            conexao.rollback(); // só leitura: encerra a transação
        }
    }

    @GetMapping
    public synchronized List<CatequizandoResponse> buscar(@RequestParam(required = false) String nome,
                                                          @RequestParam(required = false) String responsavel) throws SQLException {
        Conexao conexao = Conexao.getInstancia();
        try {
            List<Catequizando> lista = new Catequizando().buscar(nome, responsavel, conexao);
            return lista.stream().map(this::converter).toList();
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

    private void preencherOpcionais(Catequizando c, CatequizandoRequest request) {
        c.setEmail(request.email());
        c.setEndereco(request.endereco());
        c.setBairro(request.bairro());
        c.setCidade(request.cidade());
        c.setUf(request.uf());
        c.setCep(request.cep());
        c.setEmailResponsavel(request.emailResponsavel());
        c.setTelefone(request.telefone());
        c.setSexo(vazio(request.sexo()) ? null : Sexo.valueOf(request.sexo().trim().toUpperCase()));
    }

    private ResponseEntity<?> naoEncontrado() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erros", List.of("Catequizando não encontrado")));
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private Date converter(LocalDate data) {
        return data == null ? null : java.sql.Date.valueOf(data);
    }

    private CatequizandoResponse converter(Catequizando c) {
        LocalDate nascimento = c.getDataNascimento() == null ? null
                : new java.sql.Date(c.getDataNascimento().getTime()).toLocalDate();
        return new CatequizandoResponse(c.getId(), c.getNome(), nascimento,
                c.getNomeResponsavel(), c.getTelefoneResponsavel(), c.getDocumento(),
                c.getEmail(), c.getEndereco(), c.getBairro(), c.getCidade(), c.getUf(),
                c.getCep(), c.getEmailResponsavel(), c.getTelefone(),
                c.getSexo() == null ? null : c.getSexo().name());
    }
}
