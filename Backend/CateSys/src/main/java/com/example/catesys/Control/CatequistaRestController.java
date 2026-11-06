
package com.example.catesys.Control;

import com.example.catesys.Model.Catequista;
import com.example.catesys.Model.Secretaria;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/catequistas")
public class CatequistaRestController {

    private final GerenciarCatequistaControl control =
            new GerenciarCatequistaControl();

    public record CatequistaRequest(
            String nome,
            String cpf,
            String telefone,
            String email,
            Integer secretariaId,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {}

    public record CatequistaResponse(
            int id,
            String nome,
            String cpf,
            String telefone,
            String email,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {}

    private Catequista montarCatequista(
            CatequistaRequest dados, Integer id) {

        if (dados == null) {
            throw new IllegalArgumentException("Dados obrigatorios");
        }

        Catequista catequista;

        if (id == null) {
            Secretaria secretaria = dados.secretariaId() == null
                    ? null
                    : Secretaria.reconstituir(dados.secretariaId());

            catequista = new Catequista(
                    dados.nome(),
                    dados.cpf(),
                    dados.telefone(),
                    dados.email(),
                    secretaria
            );

        } else {
            catequista = Catequista.reconstituir(
                    id,
                    dados.nome(),
                    dados.cpf(),
                    dados.telefone(),
                    dados.email(),
                    dados.secretariaId(),
                    converterData(dados.dataInicio()),
                    converterData(dados.dataFim())
            );
        }

        catequista.setDataInicio(
                converterData(dados.dataInicio())
        );

        catequista.setDataFim(
                converterData(dados.dataFim())
        );

        return catequista;
    }

    private Date converterData(LocalDate data) {
        return data == null ? null : Date.valueOf(data);
    }

    private LocalDate converterLocalDate(java.util.Date data) {
        if (data == null) {
            return null;
        }

        return new Date(data.getTime()).toLocalDate();
    }

    private CatequistaResponse converterResposta(Catequista c) {
        return new CatequistaResponse(
                c.getId(),
                c.getNome(),
                c.getCpf(),
                c.getTelefone(),
                c.getEmail(),
                converterLocalDate(c.getDataInicio()),
                converterLocalDate(c.getDataFim())
        );
    }

    @PostMapping
    public ResponseEntity<CatequistaResponse> cadastrar(
            @RequestBody CatequistaRequest dados)
            throws SQLException {

        Catequista catequista = montarCatequista(dados, null);

        if (!control.cadastrar(catequista)) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }

        Catequista salvo =
                control.buscarPorCpf(catequista.getCpf());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(converterResposta(salvo));
    }

    @GetMapping
    public List<CatequistaResponse> buscarTodos()
            throws SQLException {

        return control.buscarTodos()
                .stream()
                .map(this::converterResposta)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatequistaResponse> buscarPorId(
            @PathVariable int id)
            throws SQLException {

        Catequista catequista = control.buscarPorId(id);

        if (catequista == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                converterResposta(catequista)
        );
    }

    @GetMapping("/nome")
    public List<CatequistaResponse> buscarPorNome(
            @RequestParam String nome)
            throws SQLException {

        return control.buscarPorNome(nome)
                .stream()
                .map(this::converterResposta)
                .toList();
    }

    @GetMapping("/cpf")
    public ResponseEntity<CatequistaResponse> buscarPorCpf(
            @RequestParam String cpf)
            throws SQLException {

        Catequista catequista = control.buscarPorCpf(cpf);

        if (catequista == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                converterResposta(catequista)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CatequistaResponse> alterar(
            @PathVariable int id,
            @RequestBody CatequistaRequest dados)
            throws SQLException {

        Catequista original = control.buscarPorId(id);

        if (original == null) {
            return ResponseEntity.notFound().build();
        }

        Catequista catequista = montarCatequista(dados, id);

        catequista.setSecretaria(original.getSecretaria());

        if (!control.alterar(catequista)) {
            return ResponseEntity.notFound().build();
        }

        Catequista atualizado = control.buscarPorId(id);

        return ResponseEntity.ok(
                converterResposta(atualizado)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable int id)
            throws SQLException {

        if (!control.excluir(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> erroValidacao(
            IllegalArgumentException e) {

        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<String> erroBanco(SQLException e) {

        if ("23505".equals(e.getSQLState())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ja existe um registro com esses dados");
        }

        if ("23503".equals(e.getSQLState())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Operacao impedida por vinculos existentes");
        }

        return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
        ).body("Erro ao acessar o banco de dados");
    }
}