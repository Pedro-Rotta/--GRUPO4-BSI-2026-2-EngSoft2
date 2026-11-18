package com.example.catesys.Control;

import com.example.catesys.DAO.Conexao;
import com.example.catesys.Model.Matricula;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/matriculas")
public class GerenciarMatriculaControl {

    // Secretária provisório "admin" do banco. Quando o login existir, trocar pela secretária logada.
    private static final int SECRETARIA_PROVISORIA = 3;

    // ---------- Listas para preencher as telas ----------

    // Catequizandos que ainda podem ser matriculados
    @GetMapping("/catequizandos-disponiveis")
    public synchronized ResponseEntity<?> catequizandosDisponiveis() throws SQLException {
        Conexao conexao = Conexao.getInstancia();                           // a Control abre a conexão
        Matricula model = new Matricula(conexao.getConnection());           // a Control instancia a Model
        try {
            return ResponseEntity.ok(model.listarCatequizandosSemMatriculaAtiva());
        } finally {
            conexao.rollback();   // só leitura: encerra a transação
        }
    }

    // Turmas que ainda têm vaga
    @GetMapping("/turmas-com-vaga")
    public synchronized ResponseEntity<?> turmasComVaga() throws SQLException {
        Conexao conexao = Conexao.getInstancia();
        Matricula model = new Matricula(conexao.getConnection());
        try {
            return ResponseEntity.ok(model.listarTurmasComVaga());
        } finally {
            conexao.rollback();
        }
    }

    // Catequizandos matriculados, com a turma de cada um (para desmatricular)
    @GetMapping("/catequizandos-matriculados")
    public synchronized ResponseEntity<?> catequizandosMatriculados() throws SQLException {
        Conexao conexao = Conexao.getInstancia();
        Matricula model = new Matricula(conexao.getConnection());
        try {
            return ResponseEntity.ok(model.listarCatequizandosComMatriculaAtiva());
        } finally {
            conexao.rollback();
        }
    }

    // ---------- F1: Matricular ----------

    @PostMapping
    public synchronized ResponseEntity<?> matricular(@RequestBody Map<String, Object> dados) throws SQLException {
        Conexao conexao = Conexao.getInstancia();
        Matricula model = new Matricula(conexao.getConnection());
        try {
            Map<String, String> erros = model.matricular(
                    inteiro(dados.get("catequizandoId")), inteiro(dados.get("turmaId")), SECRETARIA_PROVISORIA);
            if (!erros.isEmpty()) {
                conexao.rollback();
                return ResponseEntity.badRequest().body(Map.of("erros", erros));
            }
            conexao.commit();
        } catch (SQLException | RuntimeException e) {
            conexao.rollback();
            throw e;
        }

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("mensagem", "Matrícula realizada com sucesso");
        resposta.put("matriculaId", model.getId());
        resposta.put("catequizandoId", model.getCatequizandoId());
        resposta.put("turmaId", model.getTurmaId());
        resposta.put("dataMatricula", formatar(model.getDataMatricula()));
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    // ---------- F2: Desmatricular ----------

    @PostMapping("/desmatricular")
    public synchronized ResponseEntity<?> desmatricular(@RequestBody Map<String, Object> dados) throws SQLException {
        Conexao conexao = Conexao.getInstancia();
        Matricula model = new Matricula(conexao.getConnection());
        try {
            Object motivo = dados.get("motivo");
            Map<String, String> erros = model.desmatricular(
                    inteiro(dados.get("catequizandoId")), motivo == null ? null : motivo.toString());
            if (!erros.isEmpty()) {
                conexao.rollback();
                return ResponseEntity.badRequest().body(Map.of("erros", erros));
            }
            conexao.commit();
        } catch (SQLException | RuntimeException e) {
            conexao.rollback();
            throw e;
        }

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("mensagem", "Matrícula encerrada com sucesso");
        resposta.put("matriculaId", model.getId());
        resposta.put("catequizandoId", model.getCatequizandoId());
        resposta.put("turmaId", model.getTurmaId());
        resposta.put("dataDesistencia", formatar(model.getDataDesistencia()));
        return ResponseEntity.ok(resposta);
    }

    private int inteiro(Object valor) {
        if (valor instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(valor).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // A data vai na resposta como "yyyy-MM-dd"
    private String formatar(Date data) {
        return new SimpleDateFormat("yyyy-MM-dd").format(data);
    }
}