package com.example.catesys.DAO;

import com.example.catesys.Control.Conexao;
import com.example.catesys.Model.Catequista;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CatequistaDAO implements IDAO<Catequista> {
    private static final String SELECT_CATEQUISTA = """
            SELECT catequista_id, nome, cpf, telefone, email, secretaria_id, data_inicio, data_fim
            FROM catequista
            """;

    private final Conexao conexao;

    public CatequistaDAO(Conexao conexao) {
        this.conexao = conexao;
    }

    @Override
    public boolean gravar(Catequista c) throws SQLException {
        String sql = """
                INSERT INTO catequista (nome, cpf, telefone, email, secretaria_id, data_inicio, data_fim, senha)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            preencherDados(ps, c);
            ps.setString(8, c.getSenha());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean alterar(Catequista c) throws SQLException {
        String sql = """
                UPDATE catequista SET nome = ?, cpf = ?, telefone = ?, email = ?,
                secretaria_id = ?, data_inicio = ?, data_fim = ?
                WHERE catequista_id = ?
                """;
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            preencherDados(ps, c);
            ps.setInt(8, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean excluir(int id) throws SQLException {
        String sql = "DELETE FROM catequista WHERE catequista_id = ?";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Catequista buscarPorId(int id) throws SQLException {
        String sql = SELECT_CATEQUISTA + " WHERE catequista_id = ?";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? lerCatequista(rs) : null;
            }
        }
    }

    @Override
    public List<Catequista> buscarTodos() throws SQLException {
        return buscar(null, null);
    }

    // Filtros opcionais: parte do nome e parte do CPF (só dígitos). Sem filtro, lista todos.
    public List<Catequista> buscar(String nome, String cpf) throws SQLException {
        String sql = SELECT_CATEQUISTA + """
                WHERE (CAST(? AS varchar) IS NULL OR nome ILIKE '%' || ? || '%')
                  AND (CAST(? AS varchar) IS NULL OR cpf LIKE '%' || ? || '%')
                ORDER BY nome
                """;
        String filtroNome = vazio(nome) ? null : nome.trim();
        String filtroCpf = vazio(cpf) ? null : cpf.replaceAll("\\D", "");
        List<Catequista> lista = new ArrayList<>();
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setString(1, filtroNome);
            ps.setString(2, filtroNome);
            ps.setString(3, filtroCpf);
            ps.setString(4, filtroCpf);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(lerCatequista(rs));
            }
        }
        return lista;
    }

    // Existe outro catequista (id diferente) com este CPF?
    public boolean existeCpf(String cpf, int idIgnorado) throws SQLException {
        String sql = "SELECT 1 FROM catequista WHERE cpf = ? AND catequista_id <> ? LIMIT 1";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setString(1, cpf);
            ps.setInt(2, idIgnorado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean possuiTurma(int id) throws SQLException {
        String sql = "SELECT 1 FROM turma_catequista WHERE catequista_catequista_id = ? LIMIT 1";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Parâmetros 1 a 7, comuns ao INSERT e ao UPDATE.
    private void preencherDados(PreparedStatement ps, Catequista c) throws SQLException {
        ps.setString(1, c.getNome());
        ps.setString(2, c.getCpf());
        ps.setString(3, c.getTelefone());
        ps.setString(4, c.getEmail());
        if (c.getSecretariaId() == null)
            ps.setNull(5, Types.INTEGER);
        else
            ps.setInt(5, c.getSecretariaId());
        definirData(ps, 6, c.getDataInicio());
        definirData(ps, 7, c.getDataFim());
    }

    private Catequista lerCatequista(ResultSet rs) throws SQLException {
        int secretaria = rs.getInt("secretaria_id");
        Integer secretariaId = rs.wasNull() ? null : secretaria;
        return Catequista.reconstituir(
                rs.getInt("catequista_id"),
                rs.getString("nome"),
                rs.getString("cpf"),
                rs.getString("telefone"),
                rs.getString("email"),
                secretariaId,
                rs.getDate("data_inicio"),
                rs.getDate("data_fim"));
    }

    private void definirData(PreparedStatement ps, int posicao, Date data) throws SQLException {
        if (data != null)
            ps.setDate(posicao, new java.sql.Date(data.getTime()));
        else
            ps.setNull(posicao, Types.DATE);
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
