package com.example.catesys.DAO;

import com.example.catesys.Model.Frequencia;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class FrequenciaDAO {
    private final DataSource dataSource;

    public FrequenciaDAO(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    public boolean gravar(Frequencia f) throws SQLException {
        String sql = "INSERT INTO Frequencia " +
                "(data, turma_id) " +
                "VALUES (?, ?)";

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, f.getData().toString());
            ps.setInt(2, f.getTurmaId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean alterar(Frequencia f) throws SQLException {
        String sql = "UPDATE Frequencia SET data = ?, turma_id = ? " +
                "WHERE data = ? AND turma_id = ?";

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setDate(1, (Date) f.getData());
            ps.setInt(2, f.getTurmaId());
            ps.setDate(3, (Date) f.getData());
            ps.setInt(4, f.getTurmaId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean excluir(Frequencia f) throws SQLException {
        String sql = "DELETE FROM frequencia WHERE data = ? AND turma_id = ?";

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setDate(1, (Date) f.getData());
            ps.setInt(2, f.getTurmaId());
            return ps.executeUpdate() > 0;
        }
    }

    public Frequencia buscar(Frequencia f) throws SQLException {
        String sql = "SELECT data, turma_id " +
                "FROM frequencia WHERE data = ? AND turma_id = ?";

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setDate(1, (Date) f.getData());
            ps.setInt(2, f.getTurmaId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return lerFrequencia(rs);
                }
            }
        }
        return null;
    }

    public List<Frequencia> buscarTodos() throws SQLException {
        String sql = "SELECT data, turma_id " +
                "FROM frequencia ORDER BY data";
        List<Frequencia> lista = new ArrayList<>();

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(lerFrequencia(rs));
            }
        }
        return lista;
    }

    private Frequencia lerFrequencia(ResultSet rs) throws SQLException {
        return Frequencia.reconstituir(
                rs.getDate("data"),
                rs.getInt("turma_id")
        );
    }
}
