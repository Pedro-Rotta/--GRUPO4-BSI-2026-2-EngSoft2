
package com.example.catesys.Control;

import com.example.catesys.Model.Turma;
import com.example.catesys.Model.TurmaCatequista;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.catesys.Model.Enums.Etapa;
import com.example.catesys.Model.Enums.StatusTurma;
import java.time.LocalDate;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/turmas")
public class TurmaRestController {

    private final ConsultarTurmaControl consultaControl =
            new ConsultarTurmaControl();

    private final TrocarCatequistaTurmaControl trocaControl =
            new TrocarCatequistaTurmaControl();

    private final CadastrarTurmaControl cadastroControl =
            new CadastrarTurmaControl();

    // DADOS DA LISTAGEM

    public record TurmaResponse(
            int id,
            String etapa,
            String anoOferta,
            String status
    ) {}

    // DETALHES DE UMA TURMA

    public record TurmaDetalheResponse(
            int id,
            String etapa,
            String anoOferta,
            String status,
            int horarioId,
            int localId,
            List<Integer> catequistasIds
    ) {}

    // DADOS RECEBIDOS PARA A TROCA

    public record TrocaCatequistaRequest(
            Integer catequistaAtualId,
            Integer novoCatequistaId
    ) {}

    // LISTAR TODAS AS TURMAS

    @GetMapping
    public List<TurmaResponse> buscarTodos()
            throws SQLException {

        return consultaControl.buscarTodos()
                .stream()
                .map(this::converterResposta)
                .toList();
    }

    // CONSULTAR TURMA POR ID

    @GetMapping("/{id}")
    public ResponseEntity<TurmaDetalheResponse> buscarPorId(
            @PathVariable int id
    ) throws SQLException {

        ConsultarTurmaControl.DetalhesTurma detalhes =
                consultaControl.buscarPorId(id);

        if (detalhes == null) {
            return ResponseEntity.notFound().build();
        }

        Turma turma = detalhes.turma();

        List<Integer> catequistasIds =
                detalhes.vinculos()
                        .stream()
                        .map(TurmaCatequista::getCatequistaId)
                        .toList();

        TurmaDetalheResponse resposta =
                new TurmaDetalheResponse(
                        turma.getId(),
                        turma.getEtapa() == null
                                ? null
                                : turma.getEtapa().name(),
                        turma.getAnoOferta(),
                        turma.getStatus() == null
                                ? null
                                : turma.getStatus().name(),
                        turma.getHorarioId(),
                        turma.getLocalId(),
                        catequistasIds
                );

        return ResponseEntity.ok(resposta);
    }

    // TROCAR CATEQUISTA DA TURMA

    @PutMapping("/{id}/catequista")
    public ResponseEntity<Void> trocarCatequista(
            @PathVariable int id,
            @RequestBody TrocaCatequistaRequest dados
    ) throws SQLException {

        if (dados == null ||
                dados.catequistaAtualId() == null ||
                dados.novoCatequistaId() == null) {

            throw new IllegalArgumentException(
                    "Informe o catequista atual e o novo catequista"
            );
        }

        boolean alterou = trocaControl.trocarCatequista(
                id,
                dados.catequistaAtualId(),
                dados.novoCatequistaId()
        );

        if (!alterou) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }

        return ResponseEntity.noContent().build();
    }

    // CONVERTER MODEL PARA RESPOSTA

    private TurmaResponse converterResposta(Turma turma) {

        return new TurmaResponse(
                turma.getId(),
                turma.getEtapa() == null
                        ? null
                        : turma.getEtapa().name(),
                turma.getAnoOferta(),
                turma.getStatus() == null
                        ? null
                        : turma.getStatus().name()
        );
    }

// DADOS RECEBIDOS DO CADASTRO DE TURMA

    public record CadastroTurmaRequest(
            String etapa,
            String anoOferta,
            String dataInicio,
            String dataFim,
            Integer vagasTotais,
            Integer vagasDisponiveis,
            String status,
            Integer localId,
            Integer horarioId,
            Integer catequistaId
    ) {}

// CADASTRAR TURMA E VINCULAR CATEQUISTA

    @PostMapping
    public ResponseEntity<Map<String, Integer>> cadastrar(
            @RequestBody CadastroTurmaRequest dados
    ) throws SQLException {

        if (dados == null ||
                dados.etapa() == null ||
                dados.anoOferta() == null ||
                dados.dataInicio() == null ||
                dados.dataFim() == null ||
                dados.vagasTotais() == null ||
                dados.status() == null ||
                dados.localId() == null ||
                dados.horarioId() == null ||
                dados.catequistaId() == null) {

            throw new IllegalArgumentException(
                    "Preencha todos os campos obrigatórios");
        }

        Turma turma = new Turma();

        try {
            turma.setEtapa(
                    Etapa.valueOf(dados.etapa()));

            turma.setStatus(
                    StatusTurma.valueOf(dados.status()));

            turma.setDataInicio(
                    java.sql.Date.valueOf(
                            LocalDate.parse(dados.dataInicio())));

            turma.setDataFim(
                    java.sql.Date.valueOf(
                            LocalDate.parse(dados.dataFim())));

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Etapa, status ou datas inválidos");
        }

        turma.setAnoOferta(dados.anoOferta());
        turma.setVagasTotais(dados.vagasTotais());
        turma.setVagasDisponiveis(
                dados.vagasDisponiveis() == null
                        ? dados.vagasTotais()
                        : dados.vagasDisponiveis()
        );

        turma.setLocalId(dados.localId());
        turma.setHorarioId(dados.horarioId());

        // Coluna opcional: gravada como NULL no DAO.
        turma.setLocalHorarioid(0);

        int id = cadastroControl.cadastrar(
                turma,
                dados.catequistaId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }


    // ERROS DE VALIDACAO

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> erroValidacao(
            IllegalArgumentException e
    ) {

        return ResponseEntity.badRequest()
                .body(Map.of(
                        "message",
                        e.getMessage()
                ));
    }

    // ERROS DO BANCO

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, String>> erroBanco(
            SQLException e
    ) {

        if ("23505".equals(e.getSQLState())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "Esse vinculo ja existe"
                    ));
        }

        if ("23503".equals(e.getSQLState())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "Turma ou catequista nao encontrado"
                    ));
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "message",
                        "Erro ao realizar a operacao no banco"
                ));
    }
}
