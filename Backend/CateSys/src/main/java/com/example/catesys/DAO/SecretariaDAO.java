package com.example.catesys.DAO;

import com.example.catesys.Control.Conexao;
import com.example.catesys.Model.Secretaria;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SecretariaDAO implements IDAO<Secretaria> {
    private static final String SELECT_SECRETARIA = """
            SELECT secretaria_id, nome, cpf, email, senha, telefone, data_admissao, data_demissao
            FROM secretaria
            """;

    private final Conexao conexao;

    public SecretariaDAO(Conexao conexao) {
        this.conexao = conexao;
    }

    @Override
    public boolean gravar(Secretaria s) throws SQLException {
        String sql = """
                INSERT INTO secretaria (nome, cpf, email, senha, telefone, data_admissao, data_demissao)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            preencherDados(ps, s);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean alterar(Secretaria s) throws SQLException {
        String sql = """
                UPDATE secretaria SET nome = ?, cpf = ?, email = ?, senha = ?, telefone = ?,
                data_admissao = ?, data_demissao = ?
                WHERE secretaria_id = ?
                """;
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            preencherDados(ps, s);
            ps.setInt(8, s.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean excluir(int id) throws SQLException {
        String sql = "DELETE FROM secretaria WHERE secretaria_id = ?";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Secretaria buscarPorId(int id) throws SQLException {
        String sql = SELECT_SECRETARIA + " WHERE secretaria_id = ?";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? lerSecretaria(rs) : null;
            }
        }
    }

    @Override
    public List<Secretaria> buscarTodos() throws SQLException {
        String sql = SELECT_SECRETARIA + " ORDER BY nome";
        List<Secretaria> lista = new ArrayList<>();
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                lista.add(lerSecretaria(rs));
        }
        return lista;
    }

    // O login pode ser o e-mail ou o CPF (só dígitos).
    public Secretaria buscarPorLogin(String login) throws SQLException {
        if (login == null || login.isBlank())
            return null;
        String sql = SELECT_SECRETARIA + " WHERE LOWER(email) = LOWER(?) OR cpf = ? LIMIT 1";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setString(1, login.trim());
            ps.setString(2, login.replaceAll("\\D", ""));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? lerSecretaria(rs) : null;
            }
        }
    }

    public boolean existeAlguma() throws SQLException {
        try (PreparedStatement ps = conexao.getConnection().prepareStatement("SELECT 1 FROM secretaria LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }

    private void preencherDados(PreparedStatement ps, Secretaria s) throws SQLException {
        ps.setString(1, s.getNome());
        ps.setString(2, s.getCpf());
        ps.setString(3, s.getEmail());
        ps.setString(4, s.getSenha());
        ps.setString(5, s.getTelefone());
        definirData(ps, 6, s.getDataAdmissao());
        definirData(ps, 7, s.getDataDemissao());
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
                rs.getDate("data_demissao"));
    }

    private void definirData(PreparedStatement ps, int posicao, Date data) throws SQLException {
        if (data != null)
            ps.setDate(posicao, new java.sql.Date(data.getTime()));
        else
            ps.setNull(posicao, Types.DATE);
    }
}
