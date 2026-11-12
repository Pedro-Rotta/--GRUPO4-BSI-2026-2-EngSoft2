package com.example.catesys.DAO;

import com.example.catesys.Model.Catequista;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class CatequistaDAO implements IDAO<Catequista> {
    private final Connection conexao;

    public CatequistaDAO(Connection conexao) {
        this.conexao = Objects.requireNonNull(conexao);
    }

    @Override
    public boolean gravar(Catequista c) throws SQLException {
        String sql = "INSERT INTO catequista " +
                "(nome, cpf, telefone, email, secretaria_id, data_inicio, data_fim, senha) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            ps.setString(2, c.getCpf());
            ps.setString(3, c.getTelefone());
            ps.setString(4, c.getEmail());
            definirSecretaria(ps, 5, c);
            definirData(ps, 6, c.getDataInicio());
            definirData(ps, 7, c.getDataFim());
            ps.setString(8, c.getSenha()); // Receber um hash, nunca a senha original.
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean alterar(Catequista c) throws SQLException {
        String sql = "UPDATE catequista SET nome = ?, cpf = ?, telefone = ?, " +
                "email = ?, secretaria_id = ?, data_inicio = ?, data_fim = ? " +
                "WHERE catequista_id = ?";

        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            ps.setString(2, c.getCpf());
            ps.setString(3, c.getTelefone());
            ps.setString(4, c.getEmail());
            definirSecretaria(ps, 5, c);
            definirData(ps, 6, c.getDataInicio());
            definirData(ps, 7, c.getDataFim());
            ps.setInt(8, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean excluir(int id) throws SQLException {
        String sql = "DELETE FROM catequista WHERE catequista_id = ?";

        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Catequista buscarPorId(int id) throws SQLException {
        String sql = "SELECT catequista_id, nome, cpf, telefone, email, " +
                "secretaria_id, data_inicio, data_fim " +
                "FROM catequista WHERE catequista_id = ?";

        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return lerCatequista(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Catequista> buscarTodos() throws SQLException {
        String sql = "SELECT catequista_id, nome, cpf, telefone, email, " +
                "secretaria_id, data_inicio, data_fim " +
                "FROM catequista ORDER BY nome";
        List<Catequista> lista = new ArrayList<>();

        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(lerCatequista(rs));
            }
        }
        return lista;
    }

    private Catequista lerCatequista(ResultSet rs) throws SQLException {
        int idSecretaria = rs.getInt("secretaria_id");
        Integer secretariaId = rs.wasNull() ? null : idSecretaria;

        // A própria Model cria os objetos; a DAO somente entrega os dados.
        return Catequista.reconstituir(
                rs.getInt("catequista_id"),
                rs.getString("nome"),
                rs.getString("cpf"),
                rs.getString("telefone"),
                rs.getString("email"),
                secretariaId,
                rs.getDate("data_inicio"),
                rs.getDate("data_fim")
        );
    }

    private void definirSecretaria(PreparedStatement ps, int posicao, Catequista c)
            throws SQLException {
        if (c.getSecretaria() == null) {
            ps.setNull(posicao, Types.INTEGER);
        } else {
            ps.setInt(posicao, c.getSecretaria().getId());
        }
    }

    private void definirData(PreparedStatement ps, int posicao, Date data)
            throws SQLException {

        if (data != null) {
            ps.setDate(posicao, new java.sql.Date(data.getTime()));
        } else {
            ps.setNull(posicao, Types.DATE);
        }
    }

    public List<Catequista> buscarPorNome(String nome) throws SQLException {
        String sql = "SELECT catequista_id, nome, cpf, telefone, email, " +
                "secretaria_id, data_inicio, data_fim " +
                "FROM catequista WHERE nome ILIKE ? ORDER BY nome";

        List<Catequista> lista = new ArrayList<>();

        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, "%" + nome.trim() + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(lerCatequista(rs));
                }
            }
        }

        return lista;
    }

    public Catequista buscarPorCpf(String cpf) throws SQLException {
        String sql = "SELECT catequista_id, nome, cpf, telefone, email, " +
                "secretaria_id, data_inicio, data_fim " +
                "FROM catequista WHERE cpf = ?";

        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, cpf.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return lerCatequista(rs);
                }
            }
        }

        return null;
    }

    public Catequista buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT catequista_id, nome, cpf, telefone, email, " +
                "secretaria_id, data_inicio, data_fim " +
                "FROM catequista WHERE email = ?";

        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, email.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return lerCatequista(rs);
                }
            }
        }

        return null;
    }

}
