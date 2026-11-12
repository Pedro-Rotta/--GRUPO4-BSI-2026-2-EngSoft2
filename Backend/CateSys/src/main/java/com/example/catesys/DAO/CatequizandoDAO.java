package com.example.catesys.DAO;

import com.example.catesys.Control.Conexao;
import com.example.catesys.Model.Catequizando;
import com.example.catesys.Model.Enums.Sexo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CatequizandoDAO implements IDAO<Catequizando> {
    private final Conexao conexao;

    public CatequizandoDAO(Conexao conexao) {
        this.conexao = conexao;
    }

    @Override
    public boolean gravar(Catequizando catequizando) throws SQLException {
        String sql = """
                INSERT INTO catequizando (nome, data_nascimento, nome_responsavel, telefone_responsavel,
                documento, email, endereco, bairro, cidade, uf, cep, email_resp, telefone, sexo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            preencherCampos(ps, catequizando);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean alterar(Catequizando catequizando) throws SQLException {
        String sql = """
                UPDATE catequizando SET nome = ?, data_nascimento = ?, nome_responsavel = ?,
                telefone_responsavel = ?, documento = ?, email = ?, endereco = ?, bairro = ?,
                cidade = ?, uf = ?, cep = ?, email_resp = ?, telefone = ?, sexo = ?
                WHERE catequizando_id = ?
                """;
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            preencherCampos(ps, catequizando);
            ps.setInt(15, catequizando.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean excluir(int id) throws SQLException {
        String sql = "DELETE FROM catequizando WHERE catequizando_id = ?";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Catequizando buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM catequizando WHERE catequizando_id = ?";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return lerCatequizando(rs);
            }
        }
        return null;
    }

    @Override
    public List<Catequizando> buscarTodos() throws SQLException {
        String sql = "SELECT * FROM catequizando ORDER BY nome";
        List<Catequizando> lista = new ArrayList<>();
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                lista.add(lerCatequizando(rs));
        }
        return lista;
    }

    public List<Catequizando> buscar(String filtroNome, String filtroResponsavel) throws SQLException {
        String sql = """
                SELECT * FROM catequizando
                WHERE nome ILIKE ? AND nome_responsavel ILIKE ?
                ORDER BY nome
                """;
        List<Catequizando> lista = new ArrayList<>();
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setString(1, comoTrecho(filtroNome));
            ps.setString(2, comoTrecho(filtroResponsavel));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(lerCatequizando(rs));
            }
        }
        return lista;
    }

    public boolean possuiMatricula(int id) throws SQLException {
        String sql = "SELECT 1 FROM matricula WHERE catequizando_id = ? LIMIT 1";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void preencherCampos(PreparedStatement ps, Catequizando c) throws SQLException {
        ps.setString(1, c.getNome());
        definirData(ps, 2, c.getDataNascimento());
        ps.setString(3, c.getNomeResponsavel());
        ps.setString(4, c.getTelefoneResponsavel());
        ps.setString(5, c.getDocumento());
        ps.setString(6, c.getEmail());
        ps.setString(7, c.getEndereco());
        ps.setString(8, c.getBairro());
        ps.setString(9, c.getCidade());
        ps.setString(10, c.getUf());
        ps.setString(11, c.getCep());
        ps.setString(12, c.getEmailResponsavel());
        ps.setString(13, c.getTelefone());
        ps.setString(14, c.getSexo() == null ? null : c.getSexo().name());
    }

    private Catequizando lerCatequizando(ResultSet rs) throws SQLException {
        Catequizando c = Catequizando.reconstituir(
                rs.getInt("catequizando_id"),
                rs.getString("nome"),
                rs.getDate("data_nascimento"),
                rs.getString("nome_responsavel"),
                rs.getString("telefone_responsavel")
        );
        c.setDocumento(rs.getString("documento"));
        c.setEmail(rs.getString("email"));
        c.setEndereco(rs.getString("endereco"));
        c.setBairro(rs.getString("bairro"));
        c.setCidade(rs.getString("cidade"));
        c.setUf(rs.getString("uf"));
        c.setCep(rs.getString("cep"));
        c.setEmailResponsavel(rs.getString("email_resp"));
        c.setTelefone(rs.getString("telefone"));
        c.setSexo(lerSexo(rs.getString("sexo")));
        return c;
    }

    private Sexo lerSexo(String valor) {
        if (valor == null || valor.isBlank())
            return null;
        try {
            return Sexo.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String comoTrecho(String filtro) {
        return filtro == null || filtro.isBlank() ? "%" : "%" + filtro.trim() + "%";
    }

    private void definirData(PreparedStatement ps, int posicao, Date data) throws SQLException {
        if (data != null)
            ps.setDate(posicao, new java.sql.Date(data.getTime()));
        else
            ps.setNull(posicao, Types.DATE);
    }
}
