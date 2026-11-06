
package com.example.catesys.DAO;

import com.example.catesys.Model.Catequizando;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class CatequizandoDAO implements IDAO<Catequizando> {

    private final Connection conexao;

    public CatequizandoDAO(Connection conexao) {
        this.conexao = Objects.requireNonNull(conexao);
    }

    @Override
    public boolean gravar(Catequizando c) throws SQLException {
        String sql = "INSERT INTO catequizando " +
                "(nome, data_nascimento, nome_responsavel, telefone_responsavel) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            definirData(ps, 2, c.getDataNascimento());
            ps.setString(3, c.getNomeResponsavel());
            ps.setString(4, c.getTelefoneResponsavel());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean alterar(Catequizando c) throws SQLException {
        String sql = "UPDATE catequizando SET nome = ?, " +
                "data_nascimento = ?, nome_responsavel = ?, " +
                "telefone_responsavel = ? WHERE catequizando_id = ?";

        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            definirData(ps, 2, c.getDataNascimento());
            ps.setString(3, c.getNomeResponsavel());
            ps.setString(4, c.getTelefoneResponsavel());
            ps.setInt(5, c.getId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean excluir(int id) throws SQLException {
        String sql = "DELETE FROM catequizando WHERE catequizando_id = ?";

        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, id);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Catequizando buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM catequizando WHERE catequizando_id = ?";

        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return lerCatequizando(rs);
                }
            }
        }

        return null;
    }

    @Override
    public List<Catequizando> buscarTodos() throws SQLException {
        String sql = "SELECT * FROM catequizando ORDER BY nome";
        List<Catequizando> lista = new ArrayList<>();

        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(lerCatequizando(rs));
            }
        }

        return lista;
    }

    private Catequizando lerCatequizando(ResultSet rs) throws SQLException {
        return Catequizando.reconstituir(
                rs.getInt("catequizando_id"),
                rs.getString("nome"),
                rs.getDate("data_nascimento"),
                rs.getString("nome_responsavel"),
                rs.getString("telefone_responsavel")
        );
    }

    private void definirData(PreparedStatement ps, int posicao, Date data)
            throws SQLException {

        if (data != null) {
            ps.setDate(posicao, new java.sql.Date(data.getTime()));
        } else {
            ps.setNull(posicao, Types.DATE);
        }
    }
}
