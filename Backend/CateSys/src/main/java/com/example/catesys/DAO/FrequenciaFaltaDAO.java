package com.example.catesys.DAO;

import com.example.catesys.Model.Frequencia;
import com.example.catesys.Model.FrequenciaFalta;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class FrequenciaFaltaDAO {

    private final DataSource dataSource;

    public FrequenciaFaltaDAO(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    public boolean gravar(FrequenciaFalta f) throws SQLException {
        String sql = "INSERT INTO frequencia_faltas " +
                "(frequencia_data, frequencia_turma_turma_id, matricula_matricula_id, justificativa_bonificacao) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setDate(1, (Date) f.getFrequenciaData());
            ps.setInt(2, f.getTurmaId());
            ps.setInt(3, f.getMatriculaId());
            ps.setString(4, f.getJustificativaBonificacao());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean alterar(FrequenciaFalta f) throws SQLException {
        String sql = "UPDATE frequencia_faltas SET justificativa_bonificacao = ? " +
                "WHERE frequencia_data = ? AND frequencia_turma_turma_id = ? AND matricula_matricula_id = ?";

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, f.getJustificativaBonificacao());
            ps.setDate(2, (Date) f.getFrequenciaData());
            ps.setInt(3, f.getTurmaId());
            ps.setInt(4, f.getMatriculaId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean excluir(FrequenciaFalta f) throws SQLException {
        String sql = "DELETE FROM frequencia_faltas " +
                "WHERE frequencia_data = ? AND frequencia_turma_turma_id = ? AND matricula_matricula_id = ?";

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setDate(1, (Date) f.getFrequenciaData());
            ps.setInt(2, f.getTurmaId());
            ps.setInt(3, f.getMatriculaId());
            return ps.executeUpdate() > 0;
        }
    }

    public FrequenciaFalta buscar(FrequenciaFalta f) throws SQLException {
        String sql = "SELECT frequencia_data, frequencia_turma_turma_id, matricula_matricula_id, justificativa_bonificacao " +
                "FROM frequencia_faltas WHERE frequencia_data = ? AND frequencia_turma_turma_id = ? AND matricula_matricula_id = ?";

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setDate(1, (Date) f.getFrequenciaData());
            ps.setInt(2, f.getTurmaId());
            ps.setInt(3, f.getMatriculaId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return lerFrequenciaFalta(rs);
                }
            }
        }
        return null;
    }

    public List<FrequenciaFalta> buscarTodos() throws SQLException {
        String sql = "SELECT frequencia_data, frequencia_turma_turma_id, matricula_matricula_id, justificativa_bonificacao " +
                "FROM frequencia_faltas ORDER BY frequencia_data";
        List<FrequenciaFalta> lista = new ArrayList<>();

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(lerFrequenciaFalta(rs));
            }
        }
        return lista;
    }

    private FrequenciaFalta lerFrequenciaFalta(ResultSet rs) throws SQLException {
        return FrequenciaFalta.reconstituir(
                rs.getDate("frequencia_data"),
                rs.getInt("frequencia_turma_turma_id"),
                rs.getInt("matricula_matricula_id"),
                rs.getString("justificativa_bonificacao")
        );
    }
}
