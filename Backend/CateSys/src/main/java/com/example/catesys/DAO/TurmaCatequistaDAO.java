package com.example.catesys.DAO;

import com.example.catesys.Control.Conexao;
import com.example.catesys.Model.TurmaCatequista;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Não implementa IDAO: o vínculo não tem id próprio nem CRUD completo (SOLID-I).
// Na oferta (RF_F5) o primeiro vínculo é gravado pelo TurmaDAO, junto com a turma.
public class TurmaCatequistaDAO {
    private final Conexao conexao;

    public TurmaCatequistaDAO(Conexao conexao) {
        this.conexao = conexao;
    }

    public List<TurmaCatequista> buscarPorTurma(int turmaId) throws SQLException {
        String sql = """
                SELECT catequista_catequista_id, turma_turma_id
                FROM turma_catequista
                WHERE turma_turma_id = ?
                ORDER BY catequista_catequista_id
                """;
        List<TurmaCatequista> lista = new ArrayList<>();
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, turmaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(new TurmaCatequista(rs.getInt("catequista_catequista_id"), rs.getInt("turma_turma_id")));
            }
        }
        return lista;
    }

    public boolean existeVinculo(TurmaCatequista vinculo) throws SQLException {
        String sql = "SELECT 1 FROM turma_catequista WHERE turma_turma_id = ? AND catequista_catequista_id = ?";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, vinculo.getTurmaId());
            ps.setInt(2, vinculo.getCatequistaId());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean gravar(TurmaCatequista vinculo) throws SQLException {
        String sql = "INSERT INTO turma_catequista (catequista_catequista_id, turma_turma_id) VALUES (?, ?)";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, vinculo.getCatequistaId());
            ps.setInt(2, vinculo.getTurmaId());
            return ps.executeUpdate() == 1;
        }
    }

    // Substitui somente o vínculo informado.
    public boolean trocarCatequista(TurmaCatequista vinculo, int novoCatequistaId) throws SQLException {
        String sql = """
                UPDATE turma_catequista SET catequista_catequista_id = ?
                WHERE turma_turma_id = ? AND catequista_catequista_id = ?
                """;
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, novoCatequistaId);
            ps.setInt(2, vinculo.getTurmaId());
            ps.setInt(3, vinculo.getCatequistaId());
            return ps.executeUpdate() == 1;
        }
    }
}
