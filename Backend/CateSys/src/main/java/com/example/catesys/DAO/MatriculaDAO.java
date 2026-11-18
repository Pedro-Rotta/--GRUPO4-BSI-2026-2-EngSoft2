package com.example.catesys.DAO;

import com.example.catesys.Model.Enums.StatusMatricula;
import com.example.catesys.Model.Matricula;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MatriculaDAO {

    // No banco o status fica gravado com o nome do enum (ATIVA/DESISTENTE)
    private static final String ATIVA = StatusMatricula.ATIVA.name();

    // A conexão vem pronta da Control
    private final Connection conn;

    public MatriculaDAO(Connection conn) {
        this.conn = conn;
    }

    // ---------- Consultas que a Model usa para conferir as regras ----------

    public boolean catequizandoExiste(int catequizandoId) throws SQLException {
        String sql = "SELECT 1 FROM catequizando WHERE catequizando_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, catequizandoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Retorna as vagas totais da turma, ou null se a turma não existe
    public Integer vagasTotaisDaTurma(int turmaId) throws SQLException {
        String sql = "SELECT vagas_totais FROM turma WHERE turma_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, turmaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("vagas_totais") : null;
            }
        }
    }

    public int contarAtivasDaTurma(int turmaId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM matricula WHERE turma_id = ? AND status = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, turmaId);
            ps.setString(2, ATIVA);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    // Preenche a Model recebida com a matrícula ativa do catequizando.
    // Retorna false se ele não tem matrícula ativa.
    public boolean buscarAtivaDoCatequizando(Matricula m, int catequizandoId) throws SQLException {
        String sql = "SELECT matricula_id, data_matricula, turma_id, secretaria_id FROM matricula "
                + "WHERE catequizando_id = ? AND status = ? ORDER BY matricula_id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, catequizandoId);
            ps.setString(2, ATIVA);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                m.setId(rs.getInt("matricula_id"));
                m.setDataMatricula(rs.getDate("data_matricula"));
                m.setStatus(StatusMatricula.ATIVA);
                m.setCatequizandoId(catequizandoId);
                m.setTurmaId(rs.getInt("turma_id"));
                m.setSecretariaId(rs.getInt("secretaria_id"));   // 0 se estiver vazio no banco
                return true;
            }
        }
    }

    // ---------- Gravações ----------

    // Insere a matrícula e devolve o id gerado pelo banco
    public void gravar(Matricula m) throws SQLException {
        String sql = "INSERT INTO matricula (data_matricula, status, catequizando_id, turma_id, secretaria_id) "
                + "VALUES (?, ?, ?, ?, ?) RETURNING matricula_id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(m.getDataMatricula().getTime()));
            ps.setString(2, m.getStatus().name());
            ps.setInt(3, m.getCatequizandoId());
            ps.setInt(4, m.getTurmaId());
            if (m.getSecretariaId() > 0) {
                ps.setInt(5, m.getSecretariaId());
            } else {
                ps.setNull(5, Types.INTEGER);        // a secretária é opcional no banco
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                m.setId(rs.getInt(1));
            }
        }
    }

    // Encerra a matrícula sem ser apagada
    public void encerrar(Matricula m) throws SQLException {
        String sql = "UPDATE matricula SET status = ?, data_desistencia = ?, motivo_desistencia = ? "
                + "WHERE matricula_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getStatus().name());
            ps.setDate(2, new java.sql.Date(m.getDataDesistencia().getTime()));
            ps.setString(3, vazioParaNull(m.getMotivoDesistencia()));
            ps.setInt(4, m.getId());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Matrícula não encontrada para encerrar.");
            }
        }
    }

    // Recalcula as vagas livres da turma
    public void atualizarVagas(int turmaId) throws SQLException {
        String sql = "UPDATE turma SET vagas_disponiveis = vagas_totais - "
                + "(SELECT COUNT(*) FROM matricula WHERE turma_id = ? AND status = ?) "
                + "WHERE turma_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, turmaId);
            ps.setString(2, ATIVA);
            ps.setInt(3, turmaId);
            ps.executeUpdate();
        }
    }

    // ---------- Listas para as telas  ----------

    public List<Map<String, Object>> listarCatequizandosSemMatriculaAtiva() throws SQLException {
        String sql = "SELECT c.catequizando_id, c.nome FROM catequizando c "
                + "WHERE NOT EXISTS (SELECT 1 FROM matricula m "
                + "WHERE m.catequizando_id = c.catequizando_id AND m.status = ?) "
                + "ORDER BY c.nome";
        List<Map<String, Object>> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ATIVA);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> linha = new LinkedHashMap<>();
                    linha.put("catequizandoId", rs.getInt("catequizando_id"));
                    linha.put("nome", rs.getString("nome"));
                    lista.add(linha);
                }
            }
        }
        return lista;
    }

    public List<Map<String, Object>> listarCatequizandosComMatriculaAtiva() throws SQLException {
        String sql = "SELECT c.catequizando_id, c.nome, t.turma_id, t.etapa, t.ano_oferta "
                + "FROM catequizando c "
                + "JOIN matricula m ON m.catequizando_id = c.catequizando_id AND m.status = ? "
                + "JOIN turma t ON t.turma_id = m.turma_id "
                + "ORDER BY c.nome";
        List<Map<String, Object>> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ATIVA);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> linha = new LinkedHashMap<>();
                    linha.put("catequizandoId", rs.getInt("catequizando_id"));
                    linha.put("nome", rs.getString("nome"));
                    linha.put("turmaId", rs.getInt("turma_id"));
                    linha.put("etapa", rs.getString("etapa"));
                    linha.put("anoOferta", rs.getString("ano_oferta"));
                    lista.add(linha);
                }
            }
        }
        return lista;
    }

    public List<Map<String, Object>> listarTurmasComVaga() throws SQLException {
        String sql = "SELECT * FROM (SELECT t.turma_id, t.etapa, t.ano_oferta, t.vagas_totais, "
                + "t.vagas_totais - (SELECT COUNT(*) FROM matricula m "
                + "WHERE m.turma_id = t.turma_id AND m.status = ?) AS vagas_livres FROM turma t) x "
                + "WHERE vagas_livres > 0 ORDER BY etapa, turma_id";
        List<Map<String, Object>> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ATIVA);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> linha = new LinkedHashMap<>();
                    linha.put("turmaId", rs.getInt("turma_id"));
                    linha.put("etapa", rs.getString("etapa"));
                    linha.put("anoOferta", rs.getString("ano_oferta"));
                    linha.put("vagasTotais", rs.getInt("vagas_totais"));
                    linha.put("vagasLivres", rs.getInt("vagas_livres"));
                    lista.add(linha);
                }
            }
        }
        return lista;
    }

    // Campo opcional em branco vai como NULL no banco
    private String vazioParaNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }
}