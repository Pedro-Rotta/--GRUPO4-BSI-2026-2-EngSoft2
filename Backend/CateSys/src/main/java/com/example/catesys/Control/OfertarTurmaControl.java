package com.example.catesys.Control;

import com.example.catesys.Model.Catequista;
import com.example.catesys.Model.Enums.Etapa;
import com.example.catesys.Model.Horario;
import com.example.catesys.Model.Local;
import com.example.catesys.Model.Turma;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

// RF_F5 - Ofertar Turma (catequista, horário e data, local).
@RestController
@RequestMapping("/api/turmas")
public class OfertarTurmaControl {

    public record OfertarTurmaRequest(String etapa, Integer catequistaId, LocalDate dataInicio, LocalDate dataFim,
                                      Integer localId, Integer horarioId, Integer vagas) {}

    public record OpcaoResponse(int id, String descricao) {}

    public record LocalResponse(int id, String nome, int capacidade) {}

    public record HorarioResponse(int id, String diaSemana, String horaInicio, String horaFim) {}

    public record OpcoesResponse(List<String> etapas, List<OpcaoResponse> catequistas,
                                 List<LocalResponse> locais, List<HorarioResponse> horarios) {}

    public record TurmaResumoResponse(int id, String etapa, String anoOferta, LocalDate dataInicio, LocalDate dataFim,
                                      String catequista, String local, HorarioResponse horario,
                                      int vagasTotais, int vagasDisponiveis, String status) {}

    // Passo 2: o formulário recebe as opções disponíveis.
    @GetMapping("/opcoes")
    public synchronized OpcoesResponse opcoes() throws SQLException {
        Conexao conexao = Conexao.getInstancia();
        try {
            List<String> etapas = Arrays.stream(Etapa.values()).map(Enum::name).toList();
            List<OpcaoResponse> catequistas = Catequista.buscarTodos(conexao.getConnection()).stream()
                    .filter(this::catequistaAtivo)
                    .map(c -> new OpcaoResponse(c.getId(), c.getNome()))
                    .toList();
            List<LocalResponse> locais = new Local().buscarTodos(conexao).stream()
                    .map(this::converter).toList();
            List<HorarioResponse> horarios = new Horario().buscarTodos(conexao).stream()
                    .map(OfertarTurmaControl::converter).toList();
            return new OpcoesResponse(etapas, catequistas, locais, horarios);
        } finally {
            conexao.rollback(); // só leitura: encerra a transação
        }
    }

    // Passo 4 antecipado: horários livres no local (e para o catequista, se informado) no período.
    @GetMapping("/horarios-disponiveis")
    public synchronized ResponseEntity<?> horariosDisponiveis(
            @RequestParam int localId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Integer catequistaId) throws SQLException {
        if (!dataFim.isAfter(dataInicio))
            return ResponseEntity.badRequest().body(Map.of("erros",
                    List.of("Data de término deve ser posterior à data de início")));
        Turma turma = new Turma();
        turma.setDataInicio(converter(dataInicio));
        turma.setDataFim(converter(dataFim));
        turma.setCatequistaId(valor(catequistaId));

        Conexao conexao = Conexao.getInstancia();
        try {
            if (new Local().buscarPorId(localId, conexao) == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erros", List.of("Local não encontrado")));
            List<HorarioResponse> horarios = new Horario().buscarDisponiveis(localId, turma, conexao).stream()
                    .map(OfertarTurmaControl::converter).toList();
            return ResponseEntity.ok(horarios);
        } finally {
            conexao.rollback(); // só leitura
        }
    }

    // Passos 3 a 6: valida, verifica agenda do local e do catequista, cria a turma e devolve o resumo.
    @PostMapping
    public synchronized ResponseEntity<?> ofertar(@RequestBody OfertarTurmaRequest request) throws SQLException {
        Turma turma = Turma.ofertar(
                vazio(request.etapa()) ? null : Etapa.valueOf(request.etapa().trim().toUpperCase()),
                converter(request.dataInicio()), converter(request.dataFim()),
                valor(request.vagas()), valor(request.catequistaId()),
                valor(request.localId()), valor(request.horarioId()));
        List<String> erros = turma.validar();
        if (!erros.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("erros", erros));

        Conexao conexao = Conexao.getInstancia();
        try {
            Catequista catequista = Catequista.buscarPorId(conexao.getConnection(), turma.getCatequistaId());
            Local local = new Local().buscarPorId(turma.getLocalId(), conexao);
            Horario horario = new Horario().buscarPorId(turma.getHorarioId(), conexao);
            if (catequista == null)
                return erro(conexao, HttpStatus.BAD_REQUEST, "Catequista não encontrado");
            if (local == null)
                return erro(conexao, HttpStatus.BAD_REQUEST, "Local não encontrado");
            if (horario == null)
                return erro(conexao, HttpStatus.BAD_REQUEST, "Horário não encontrado");

            if (!local.comporta(turma.getVagasTotais()))
                return erro(conexao, HttpStatus.BAD_REQUEST, "Limite de vagas (" + turma.getVagasTotais()
                        + ") maior que a capacidade do local \"" + local.getNome() + "\". Sugestão: limite as vagas a "
                        + local.getCapacidade() + ".");
            if (turma.existeConflitoLocal(conexao))
                return erro(conexao, HttpStatus.CONFLICT, "Local indisponível: \"" + local.getNome()
                        + "\" já tem turma neste dia e horário no período informado.");
            if (turma.existeConflitoCatequista(conexao))
                return erro(conexao, HttpStatus.CONFLICT, "Conflito de horário: o catequista " + catequista.getNome()
                        + " já tem turma neste dia e horário no período informado.");

            turma.gravar(conexao); // turma + vínculo com o catequista, na mesma transação
            conexao.commit();
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "mensagem", "Turma ofertada com sucesso",
                    "turma", resumo(turma, catequista.getNome(), local, horario)));
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

    @ExceptionHandler({HttpMessageNotReadableException.class, IllegalArgumentException.class,
            MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<?> requisicaoInvalida(Exception e) {
        return ResponseEntity.badRequest()
                .body(Map.of("erros", List.of("Requisição inválida: verifique os campos enviados")));
    }

    private static TurmaResumoResponse resumo(Turma t, String catequista, Local local, Horario horario) {
        return new TurmaResumoResponse(t.getId(), t.getEtapa() == null ? null : t.getEtapa().name(), t.getAnoOferta(),
                Turma.paraLocalDate(t.getDataInicio()), Turma.paraLocalDate(t.getDataFim()), catequista,
                local == null ? null : local.getNome(), horario == null ? null : converter(horario),
                t.getVagasTotais(), t.getVagasDisponiveis(), t.getStatus() == null ? null : t.getStatus().name());
    }

    private static HorarioResponse converter(Horario h) {
        return new HorarioResponse(h.getId(), h.getDiaSemana() == null ? null : h.getDiaSemana().name(),
                hora(h.getHoraInicio()), hora(h.getHoraFim()));
    }

    private LocalResponse converter(Local l) {
        return new LocalResponse(l.getId(), l.getNome(), l.getCapacidade());
    }

    private static String hora(java.sql.Time t) {
        return t == null ? null : t.toLocalTime().toString(); // "14:00"
    }

    private boolean catequistaAtivo(Catequista c) {
        return c.getDataFim() == null || c.getDataFim().after(new Date());
    }

    private ResponseEntity<?> erro(Conexao conexao, HttpStatus status, String mensagem) throws SQLException {
        conexao.rollback();
        return ResponseEntity.status(status).body(Map.of("erros", List.of(mensagem)));
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static int valor(Integer numero) {
        return numero == null ? 0 : numero;
    }

    private Date converter(LocalDate data) {
        return data == null ? null : java.sql.Date.valueOf(data);
    }
}
