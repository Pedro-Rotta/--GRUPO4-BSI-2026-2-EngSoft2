package com.example.catesys.Control;

import com.example.catesys.DAO.Conexao;
import com.example.catesys.Model.Matricula;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

// Caso de uso: desmatricular catequizando.
// O Spring cria uma única instância desta classe, então ela já funciona como Singleton.
@RestController
@RequestMapping("/api/desmatriculas")
public class DesmatricularControl {

    // Catequizandos matriculados, com a turma de cada um
    @GetMapping("/catequizandos-matriculados")
    public synchronized ResponseEntity<?> catequizandosMatriculados() throws SQLException {
        Conexao conexao = Conexao.getInstancia();                           // a Control abre a conexão
        Matricula model = new Matricula(conexao.getConnection());           // a Control instancia a Model
        try {
            return ResponseEntity.ok(model.listarCatequizandosComMatriculaAtiva());
        } finally {
            conexao.rollback();   // só leitura: encerra a transação
        }
    }

    // Recebe {"catequizandoId": 29, "motivo": "texto opcional"}. Devolve 200 se encerrou ou 400 com os erros por campo.
    @PostMapping
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
        resposta.put("dataDesistencia", new SimpleDateFormat("yyyy-MM-dd").format(model.getDataDesistencia()));
        return ResponseEntity.ok(resposta);
    }

    // O JSON pode mandar o id como número (29) ou como texto ("29"). Se não der para ler, vira 0.
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
}