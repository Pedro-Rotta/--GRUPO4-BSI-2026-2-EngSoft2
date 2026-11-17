package com.example.catesys.DAO;

import com.example.catesys.Control.Conexao;
import com.example.catesys.Model.Frequencia;
import com.example.catesys.Model.FrequenciaFalta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Não implementa IDAO: a frequência tem chave composta (data + turma) e o caso de uso só grava (SOLID-I).
public class FrequenciaDAO {
    private final Conexao conexao;

    public FrequenciaDAO(Conexao conexao) {
        this.conexao = conexao;
    }

    // Grava o encontro (frequencia) e uma linha em frequencia_faltas para cada ausente.
    public boolean gravar(Frequencia frequencia) throws SQLException {
        String sql = "INSERT INTO frequencia (data, turma_turma_id) VALUES (?, ?)";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(frequencia.getData().getTime()));
            ps.setInt(2, frequencia.getTurmaId());
            if (ps.executeUpdate() == 0)
                return false;
        }

        String sqlFalta = """
                INSERT INTO frequencia_faltas (frequencia_data, frequencia_turma_turma_id,
                matricula_matricula_id, justificativa_bonificacao)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sqlFalta)) {
            for (FrequenciaFalta falta : frequencia.getFaltas()) {
                ps.setDate(1, new java.sql.Date(falta.getFrequenciaData().getTime()));
                ps.setInt(2, falta.getTurmaId());
                ps.setInt(3, falta.getMatriculaId());
                ps.setString(4, falta.getJustificativaBonificacao());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        return true;
    }

    public boolean existe(Frequencia frequencia) throws SQLException {
        String sql = "SELECT 1 FROM frequencia WHERE data = ? AND turma_turma_id = ?";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(frequencia.getData().getTime()));
            ps.setInt(2, frequencia.getTurmaId());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Matrículas ativas da turma: só elas podem receber falta.
    public List<Integer> matriculasAtivas(int turmaId) throws SQLException {
        String sql = "SELECT matricula_id FROM matricula WHERE turma_id = ? AND UPPER(status) = 'ATIVA'";
        List<Integer> lista = new ArrayList<>();
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, turmaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(rs.getInt("matricula_id"));
            }
        }
        return lista;
    }
}
