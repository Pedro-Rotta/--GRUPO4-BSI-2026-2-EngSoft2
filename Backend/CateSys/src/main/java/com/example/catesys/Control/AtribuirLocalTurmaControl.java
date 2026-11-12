package com.example.catesys.Control;

import com.example.catesys.Model.Catequista;
import com.example.catesys.Model.Horario;
import com.example.catesys.Model.Local;
import com.example.catesys.Model.Turma;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// RF_F8 - Atribuir Local à Turma.
@RestController
@RequestMapping("/api/turmas")
public class AtribuirLocalTurmaControl {

    public record AtribuirLocalRequest(Integer localId, Integer horarioId) {}

    public record LocalResponse(int id, String nome, int capacidade) {}

    public record HorarioResponse(int id, String diaSemana, String horaInicio, String horaFim) {}

    public record TurmaResponse(int id, String etapa, String anoOferta, LocalDate dataInicio, LocalDate dataFim,
                                int vagasTotais, int vagasDisponiveis, String status,
                                Integer catequistaId, String catequista,
                                Integer localId, String local, HorarioResponse horario) {}

    public record TurmaComLocaisResponse(TurmaResponse turma, List<LocalResponse> locais) {}

    // Lista as turmas para a secretaria escolher qual alterar.
    @GetMapping
    public synchronized List<TurmaResponse> listar() throws SQLException {
        Conexao conexao = Conexao.getInstancia();
        try {
            Map<Integer, Local> locais = porId(new Local().buscarTodos(conexao), Local::getId);
            Map<Integer, Horario> horarios = porId(new Horario().buscarTodos(conexao), Horario::getId);
            Map<Integer, Catequista> catequistas = porId(Catequista.buscarTodos(conexao.getConnection()), Catequista::getId);
            return new Turma().buscarTodas(conexao).stream()
                    .map(t -> converter(t, catequistas.get(t.getCatequistaId()),
                            locais.get(t.getLocalId()), horarios.get(t.getHorarioId())))
                    .toList();
        } finally {
            conexao.rollback();
        }
    }

    // Passos 1 e 2: busca a turma e devolve o local atual e a lista de locais cadastrados.
    @GetMapping("/{id}")
    public synchronized ResponseEntity<?> buscar(@PathVariable int id) throws SQLException {
        Conexao conexao = Conexao.getInstancia();
        try {
            Turma turma = new Turma().buscarPorId(id, conexao);
            if (turma == null)
                return turmaNaoEncontrada(id);
            List<LocalResponse> locais = new Local().buscarTodos(conexao).stream().map(this::converter).toList();
            return ResponseEntity.ok(new TurmaComLocaisResponse(completa(turma, conexao), locais));
        } finally {
            conexao.rollback();
        }
    }

    // Passo 4: horários em que o local escolhido está livre no período da turma.
    @GetMapping("/{id}/locais/{localId}/horarios")
    public synchronized ResponseEntity<?> horariosDisponiveis(@PathVariable int id, @PathVariable int localId)
            throws SQLException {
        Conexao conexao = Conexao.getInstancia();
        try {
            Turma turma = new Turma().buscarPorId(id, conexao);
            if (turma == null)
                return turmaNaoEncontrada(id);
            if (new Local().buscarPorId(localId, conexao) == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erros", List.of("Local não encontrado")));
            List<HorarioResponse> horarios = new Horario().buscarDisponiveis(localId, turma, conexao).stream()
                    .map(this::converter).toList();
            return ResponseEntity.ok(horarios);
        } finally {
            conexao.rollback();
        }
    }

    // Passos 5 a 7: vincula local e horário à turma e devolve a turma completa.
    @PutMapping("/{id}/local")
    public synchronized ResponseEntity<?> atribuir(@PathVariable int id, @RequestBody AtribuirLocalRequest request)
            throws SQLException {
        Conexao conexao = Conexao.getInstancia();
        try {
            Turma turma = new Turma().buscarPorId(id, conexao);
            if (turma == null) {
                conexao.rollback();
                return turmaNaoEncontrada(id);
            }
            turma.atribuirLocal(valor(request.localId()), valor(request.horarioId()));
            List<String> erros = turma.validar();
            if (!erros.isEmpty()) {
                conexao.rollback();
                return ResponseEntity.badRequest().body(Map.of("erros", erros));
            }
            if (!turma.estaAtiva())
                return erro(conexao, HttpStatus.CONFLICT, "Turma encerrada ou cancelada não pode mudar de local");

            Local local = new Local().buscarPorId(turma.getLocalId(), conexao);
            if (local == null)
                return erro(conexao, HttpStatus.BAD_REQUEST, "Local não encontrado");
            if (new Horario().buscarPorId(turma.getHorarioId(), conexao) == null)
                return erro(conexao, HttpStatus.BAD_REQUEST, "Horário não encontrado");
            if (!local.comporta(turma.getVagasTotais()))
                return erro(conexao, HttpStatus.BAD_REQUEST, "O local \"" + local.getNome() + "\" comporta "
                        + local.getCapacidade() + " pessoas e a turma tem " + turma.getVagasTotais() + " vagas");
            if (turma.existeConflitoLocal(conexao))
                return erro(conexao, HttpStatus.CONFLICT, "Horário indisponível: o local \"" + local.getNome()
                        + "\" já tem turma neste dia e horário no período da turma");
            if (turma.getCatequistaId() > 0 && turma.existeConflitoCatequista(conexao))
                return erro(conexao, HttpStatus.CONFLICT,
                        "Conflito de horário: o catequista da turma já tem outra turma neste dia e horário");

            if (!turma.alterarLocal(conexao)) {
                conexao.rollback();
                return turmaNaoEncontrada(id);
            }
            conexao.commit();
            return ResponseEntity.ok(Map.of(
                    "mensagem", "Local atribuído à turma com sucesso",
                    "turma", completa(turma, conexao)));
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

    private TurmaResponse completa(Turma turma, Conexao conexao) throws SQLException {
        Catequista catequista = turma.getCatequistaId() > 0
                ? Catequista.buscarPorId(conexao.getConnection(), turma.getCatequistaId()) : null;
        Local local = new Local().buscarPorId(turma.getLocalId(), conexao);
        Horario horario = new Horario().buscarPorId(turma.getHorarioId(), conexao);
        return converter(turma, catequista, local, horario);
    }

    private TurmaResponse converter(Turma t, Catequista c, Local l, Horario h) {
        return new TurmaResponse(t.getId(), t.getEtapa() == null ? null : t.getEtapa().name(), t.getAnoOferta(),
                Turma.paraLocalDate(t.getDataInicio()), Turma.paraLocalDate(t.getDataFim()),
                t.getVagasTotais(), t.getVagasDisponiveis(), t.getStatus() == null ? null : t.getStatus().name(),
                c == null ? null : c.getId(), c == null ? null : c.getNome(),
                l == null ? null : l.getId(), l == null ? null : l.getNome(),
                h == null ? null : converter(h));
    }

    private LocalResponse converter(Local l) {
        return new LocalResponse(l.getId(), l.getNome(), l.getCapacidade());
    }

    private HorarioResponse converter(Horario h) {
        return new HorarioResponse(h.getId(), h.getDiaSemana() == null ? null : h.getDiaSemana().name(),
                h.getHoraInicio() == null ? null : h.getHoraInicio().toLocalTime().toString(),
                h.getHoraFim() == null ? null : h.getHoraFim().toLocalTime().toString());
    }

    private static <T> Map<Integer, T> porId(List<T> lista, Function<T, Integer> id) {
        return lista.stream().collect(Collectors.toMap(id, Function.identity(), (a, b) -> a));
    }

    private ResponseEntity<?> turmaNaoEncontrada(int id) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erros", List.of("A turma " + id + " não existe")));
    }

    private ResponseEntity<?> erro(Conexao conexao, HttpStatus status, String mensagem) throws SQLException {
        conexao.rollback();
        return ResponseEntity.status(status).body(Map.of("erros", List.of(mensagem)));
    }

    private static int valor(Integer numero) {
        return numero == null ? 0 : numero;
    }
}
