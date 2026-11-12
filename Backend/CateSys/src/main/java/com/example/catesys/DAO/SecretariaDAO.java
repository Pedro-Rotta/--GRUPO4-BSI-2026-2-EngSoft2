package com.example.catesys.DAO;

import com.example.catesys.Model.Secretaria;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class SecretariaDAO implements IDAO<Secretaria> {

    private final DataSource dataSource;

    public SecretariaDAO(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    @Override
    public boolean gravar(Secretaria s) throws SQLException {
        String sql = "INSERT INTO secretaria " +
                "(nome, cpf, email, senha, telefone, data_admissao, data_demissao) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, s.getNome());
            ps.setString(2, s.getCpf());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getSenha());
            ps.setString(5, s.getTelefone());
            setDateOrNull(ps, 6, s.getDataAdmissao());
            setDateOrNull(ps, 7, s.getDataDemissao());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean alterar(Secretaria s) throws SQLException {
        String sql = "UPDATE secretaria SET nome = ?, cpf = ?, email = ?, " +
                "senha = ?, telefone = ?, data_admissao = ?, data_demissao = ? " +
                "WHERE secretaria_id = ?";

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, s.getNome());
            ps.setString(2, s.getCpf());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getSenha());
            ps.setString(5, s.getTelefone());
            setDateOrNull(ps, 6, s.getDataAdmissao());
            setDateOrNull(ps, 7, s.getDataDemissao());
            ps.setInt(8, s.getId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean excluir(int id) throws SQLException {
        String sql = "DELETE FROM secretaria WHERE secretaria_id = ?";

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Secretaria buscarPorId(int id) throws SQLException {
        String sql = "SELECT secretaria_id, nome, cpf, email, senha, telefone, " +
                "data_admissao, data_demissao " +
                "FROM secretaria WHERE secretaria_id = ?";

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return lerSecretaria(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Secretaria> buscarTodos() throws SQLException {
        String sql = "SELECT secretaria_id, nome, cpf, email, senha, telefone, " +
                "data_admissao, data_demissao " +
                "FROM secretaria ORDER BY nome";
        List<Secretaria> lista = new ArrayList<>();

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(lerSecretaria(rs));
            }
        }
        return lista;
    }

    public Secretaria buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT secretaria_id, nome, cpf, email, senha, telefone, " +
                "data_admissao, data_demissao " +
                "FROM secretaria WHERE email = ?";

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return lerSecretaria(rs);
                }
            }
        }
        return null;
    }

    private Secretaria lerSecretaria(ResultSet rs) throws SQLException {
        return Secretaria.reconstituir(
                rs.getInt("secretaria_id"),
                rs.getString("nome"),
                rs.getString("cpf"),
                rs.getString("email"),
                rs.getString("senha"),
                rs.getString("telefone"),
                rs.getDate("data_admissao"),
                rs.getDate("data_demissao")
        );
    }

    private void setDateOrNull(PreparedStatement ps, int index, java.util.Date data) throws SQLException {
        if (data != null) {
            ps.setDate(index, new Date(data.getTime()));
        } else {
            ps.setNull(index, Types.DATE);
        }
    }
}