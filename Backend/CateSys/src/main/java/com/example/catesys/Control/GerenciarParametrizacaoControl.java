package com.example.catesys.Control;

import com.example.catesys.DAO.Conexao;
import com.example.catesys.Model.Parametrizacao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Controla o cadastro da parametrização, insere na primeira vez e altera nas próximas.
@RestController
@RequestMapping("/api/parametrizacao")
public class GerenciarParametrizacaoControl {

    // Devolve a parametrização, ou 404 se ela ainda não foi feita.
    // O 404 abre a tela de parametrização automaticamente.
    @GetMapping
    public synchronized ResponseEntity<?> buscar() throws SQLException {
        Conexao conexao = Conexao.getInstancia();                           // Control abre a conexão
        Parametrizacao model = new Parametrizacao(conexao.getConnection()); // Control instancia a Model
        try {
            if (!model.buscar()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erros", List.of("Parametrização ainda não realizada")));
            }
            return ResponseEntity.ok(converter(model));
        } finally {
            conexao.rollback();  // só leitura, encerra a transação
        }
    }

    // Se a tabela está vazia insere (201), senão altera (200).
    @PutMapping
    public synchronized ResponseEntity<?> salvar(@RequestBody Map<String, String> dados) throws SQLException {
        Conexao conexao = Conexao.getInstancia();
        Parametrizacao model = new Parametrizacao(conexao.getConnection());
        model.preencher(dados);

        // Devolve 400 se houver erros
        Map<String, String> erros = model.validar();
        if (!erros.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erros", erros));
        }

        try {
            boolean inseriu = model.salvar();
            conexao.commit();
            if (inseriu) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("mensagem", "Parametrização cadastrada com sucesso"));
            }
            return ResponseEntity.ok(Map.of("mensagem", "Parametrização atualizada com sucesso"));
        } catch (SQLException | RuntimeException e) {
            conexao.rollback();
            throw e;
        }
    }

    private Map<String, Object> converter(Parametrizacao p) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("nome", p.getNome());
        json.put("cnpj", p.getCnpj());
        json.put("responsavel", p.getResponsavel());
        json.put("telefone", p.getTelefone());
        json.put("email", p.getEmail());
        json.put("site", p.getSite());
        json.put("dataCriacao", p.getDataCriacao() == null ? null
                : new SimpleDateFormat("yyyy-MM-dd").format(p.getDataCriacao()));
        json.put("endereco", p.getEndereco());
        json.put("bairro", p.getBairro());
        json.put("cidade", p.getCidade());
        json.put("cep", p.getCep());
        json.put("uf", p.getUf());
        json.put("logotipoGrande", p.getLogotipoGrande());
        json.put("logotipoPequeno", p.getLogotipoPequeno());
        return json;
    }
}