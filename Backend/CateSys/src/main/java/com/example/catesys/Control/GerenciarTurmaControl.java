package com.example.catesys.Control;

import com.example.catesys.Model.Catequista;
import com.example.catesys.Model.Enums.Etapa;
import com.example.catesys.Model.Enums.StatusTurma;
import com.example.catesys.Model.Horario;
import com.example.catesys.Model.Local;
import com.example.catesys.Model.Turma;
import com.example.catesys.Model.TurmaCatequista;
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
import java.util.Objects;

// RF_B5 - Gerenciar Turma: consulta as turmas e altera todos os dados de uma turma
// (etapa, período, vagas, status, catequista, local e horário), conferindo a disponibilidade.
@RestController
@RequestMapping("/api/turmas")
public class GerenciarTurmaControl {

    public record AlterarTurmaRequest(String etapa, LocalDate dataInicio, LocalDate dataFim, Integer vagas,
                                      String status, Integer catequistaId, Integer localId, Integer horarioId) {}

    public record TurmaResumoResponse(int id, String etapa, String anoOferta, String status) {}

    public record HorarioResponse(int id, String diaSemana, String horaInicio, String horaFim) {}

    public record TurmaDetalheResponse(int id, String etapa, String anoOferta, LocalDate dataInicio,
                                       LocalDate dataFim, int vagasTotais, int vagasDisponiveis, int vagasOcupadas,
                                       String status, List<String> statusPossiveis,
                                       Integer catequistaId, String catequista,
                                       Integer localId, String local, Integer horarioId, HorarioResponse horario) {}

    // Lista para a tela de edição: só id, etapa, ano de oferta e status.
    @GetMapping
    public synchronized List<TurmaResumoResponse> listar() throws SQLException {
        Conexao conexao = Conexao.getInstancia();
        try {
            return new Turma().buscarTodas(conexao).stream()
                    .map(t -> new TurmaResumoResponse(t.getId(), nome(t.getEtapa()), t.getAnoOferta(),
                            nome(t.getStatus())))
                    .toList();
        } finally {
            conexao.rollback(); // só leitura: encerra a transação
        }
    }

    // Todos os dados da turma escolhida para edição.
    @GetMapping("/{id}")
    public synchronized ResponseEntity<?> buscar(@PathVariable int id) throws SQLException {
        Conexao conexao = Conexao.getInstancia();
        try {
            Turma turma = new Turma().buscarPorId(id, conexao);
            if (turma == null)
                return naoEncontrada();
            Catequista catequista = turma.getCatequistaId() > 0
                    ? new Catequista().buscarPorId(turma.getCatequistaId(), conexao) : null;
            Local local = new Local().buscarPorId(turma.getLocalId(), conexao);
            Horario horario = new Horario().buscarPorId(turma.getHorarioId(), conexao);
            return ResponseEntity.ok(detalhe(turma, catequista, local, horario));
        } finally {
            conexao.rollback();
        }
    }

    // Horários livres no local escolhido, no período e para o catequista informados,
    // sem contar a própria turma (ela pode continuar no horário atual).
    @GetMapping("/{id}/horarios-disponiveis")
    public synchronized ResponseEntity<?> horariosDisponiveis(
            @PathVariable int id,
            @RequestParam int localId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Integer catequistaId) throws SQLException {
        if (!dataFim.isAfter(dataInicio))
            return ResponseEntity.badRequest().body(Map.of("erros",
                    List.of("Data de término deve ser posterior à data de início")));
        Conexao conexao = Conexao.getInstancia();
        try {
            Turma turma = new Turma().buscarPorId(id, conexao);
            if (turma == null)
                return naoEncontrada();
            if (new Local().buscarPorId(localId, conexao) == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erros", List.of("Local não encontrado")));
            turma.setDataInicio(converter(dataInicio));
            turma.setDataFim(converter(dataFim));
            turma.setCatequistaId(catequistaId == null ? 0 : catequistaId);
            List<HorarioResponse> horarios = new Horario().buscarDisponiveis(localId, turma, conexao).stream()
                    .map(GerenciarTurmaControl::converter).toList();
            return ResponseEntity.ok(horarios);
        } finally {
            conexao.rollback();
        }
    }

    @PutMapping("/{id}")
    public synchronized ResponseEntity<?> alterar(@PathVariable int id, @RequestBody AlterarTurmaRequest request)
            throws SQLException {
        Etapa etapa = vazio(request.etapa()) ? null : Etapa.valueOf(request.etapa().trim().toUpperCase());
        StatusTurma status = vazio(request.status()) ? null : StatusTurma.valueOf(request.status().trim().toUpperCase());

        Conexao conexao = Conexao.getInstancia();
        try {
            Turma turma = new Turma().buscarPorId(id, conexao);
            if (turma == null)
                return erro(conexao, HttpStatus.NOT_FOUND, "Turma não encontrada");

            Date inicioAnterior = turma.getDataInicio();
            int catequistaAnterior = turma.getCatequistaId();
            turma.alterarDados(etapa, converter(request.dataInicio()), converter(request.dataFim()),
                    valor(request.vagas()), status, valor(request.catequistaId()),
                    valor(request.localId()), valor(request.horarioId()));

            List<String> erros = turma.validar();
            // Só a data de início alterada precisa ser hoje ou depois (turma em andamento mantém a sua).
            if (!Objects.equals(Turma.paraLocalDate(inicioAnterior), Turma.paraLocalDate(turma.getDataInicio()))
                    && turma.comecaAntesDeHoje())
                erros.add("A data de início não pode ser anterior à data atual");
            if (!erros.isEmpty()) {
                conexao.rollback();
                return ResponseEntity.badRequest().body(Map.of("erros", erros));
            }

            Catequista catequista = new Catequista().buscarPorId(turma.getCatequistaId(), conexao);
            Local local = new Local().buscarPorId(turma.getLocalId(), conexao);
            Horario horario = new Horario().buscarPorId(turma.getHorarioId(), conexao);
            if (catequista == null)
                return erro(conexao, HttpStatus.BAD_REQUEST, "Catequista não encontrado");
            if (local == null)
                return erro(conexao, HttpStatus.BAD_REQUEST, "Local não encontrado");
            if (horario == null)
                return erro(conexao, HttpStatus.BAD_REQUEST, "Horário não encontrado");
            boolean trocouCatequista = catequista.getId() != catequistaAnterior;
            if (trocouCatequista && !catequista.estaAtivo())
                return erro(conexao, HttpStatus.CONFLICT, "O catequista " + catequista.getNome() + " não está ativo");

            if (!local.comporta(turma.getVagasTotais()))
                return erro(conexao, HttpStatus.BAD_REQUEST, "Limite de vagas (" + turma.getVagasTotais()
                        + ") maior que a capacidade do local \"" + local.getNome() + "\" ("
                        + local.getCapacidade() + ").");
            // Turma encerrada ou cancelada não ocupa agenda: só confere conflito se continuar ativa.
            if (turma.estaAtiva()) {
                if (turma.existeConflitoLocal(conexao))
                    return erro(conexao, HttpStatus.CONFLICT, "Local indisponível: \"" + local.getNome()
                            + "\" já tem turma neste dia e horário no período informado.");
                if (turma.existeConflitoCatequista(conexao))
                    return erro(conexao, HttpStatus.CONFLICT, "Conflito de horário: o catequista "
                            + catequista.getNome() + " já tem turma neste dia e horário no período informado.");
            }

            turma.alterar(conexao);
            if (trocouCatequista) {
                TurmaCatequista vinculo = new TurmaCatequista(catequistaAnterior, id);
                boolean gravou = catequistaAnterior > 0 && vinculo.existe(conexao)
                        ? vinculo.trocarPor(catequista.getId(), conexao)
                        : new TurmaCatequista(catequista.getId(), id).gravar(conexao);
                if (!gravou)
                    return erro(conexao, HttpStatus.CONFLICT, "Não foi possível trocar o catequista da turma");
            }
            conexao.commit();
            return ResponseEntity.ok(Map.of("mensagem", "Turma alterada com sucesso",
                    "turma", detalhe(turma, catequista, local, horario)));
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

    private static TurmaDetalheResponse detalhe(Turma t, Catequista c, Local l, Horario h) {
        return new TurmaDetalheResponse(t.getId(), nome(t.getEtapa()), t.getAnoOferta(),
                Turma.paraLocalDate(t.getDataInicio()), Turma.paraLocalDate(t.getDataFim()),
                t.getVagasTotais(), t.getVagasDisponiveis(), t.vagasOcupadas(), nome(t.getStatus()),
                Arrays.stream(StatusTurma.values()).map(Enum::name).toList(),
                c == null ? null : c.getId(), c == null ? null : c.getNome(),
                l == null ? null : l.getId(), l == null ? null : l.getNome(),
                h == null ? null : h.getId(), h == null ? null : converter(h));
    }

    private static HorarioResponse converter(Horario h) {
        return new HorarioResponse(h.getId(), nome(h.getDiaSemana()),
                h.getHoraInicio() == null ? null : h.getHoraInicio().toLocalTime().toString(),
                h.getHoraFim() == null ? null : h.getHoraFim().toLocalTime().toString());
    }

    private ResponseEntity<?> naoEncontrada() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erros", List.of("Turma não encontrada")));
    }

    private ResponseEntity<?> erro(Conexao conexao, HttpStatus status, String mensagem) throws SQLException {
        conexao.rollback();
        return ResponseEntity.status(status).body(Map.of("erros", List.of(mensagem)));
    }

    private static String nome(Enum<?> valor) {
        return valor == null ? null : valor.name();
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static int valor(Integer numero) {
        return numero == null ? 0 : numero;
    }

    private static Date converter(LocalDate data) {
        return data == null ? null : java.sql.Date.valueOf(data);
    }
}
