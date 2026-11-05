package com.example.catesys.DAO;

import com.example.catesys.Model.Catequizando;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
public class CatequizandoDAO implements IDAO<Catequizando> {
    private final jdbcTemplate jdbc;
    public CatequizandoDAO(jdbcTemplate jdbc){
        this.jdbc = jdbc;
    }
    /*@Override
    public boolean gravar(Catequizando c) throws SQLException {
        String sql = "INSERT INTO catequizando (nome, data_nascimento, nome_responsavel, telefone_responsavel) VALUES (?, ?, ?, ?)";
        try (var ps = Conexao.getInstancia().getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            ps.setDate(2, new java.sql.Date(c.getDataNascimento().getTime()));
            ps.setString(3, c.getNomeResponsavel());
            ps.setString(4, c.getTelefoneResponsavel());
            return ps.executeUpdate() > 0;
        }
    }*/
    @Override
    public boolean gravar(Catequizando catequizando) throws SQLException{
        return jdbc.update("""INSERT INTO catequizando (nome, data_nascimento, 
                nome_responsavel, telefone_responsavel) VALUES (?, ?, ?, ?)""",
                catequizando.getNome(),
                catequizando.getDataNascimento(),
                catequizando.getNomeResponsavel(),
                catequizando.getTelefoneResponsavel()
        ) > 0;
    }
    @Override
    public boolean alterar(Catequizando catequizando) throws SQLException{
        return jdbc.update("""
                UPDATE catequizando SET nome = ?, data_nascimento = ?,
                nome_responsavel = ?, telefone_responsavel = ? WHERE id = ?
                """,
                catequizando.getNome(),
                catequizando.getDataNascimento(),
                catequizando.getNomeResponsavel(),
                catequizando.getTelefoneResponsavel(),
                catequizando.getId()
        ) > 0;
    }
    @Override
    public boolean excluir(int id) throws SQLException{
        return jdbc.update(
                "DELETE FROM catequizando WHERE id = ?",
                id
        ) > 0;
    }
    @Override
    public Catequizando buscarPorId(int id) throws SQLException{
        return jdbc.queryForObject(
                "SELECT * FROM catequizando WHERE id = ?",
                (rs, rowNum) -> {
                    Catequizando c = new Catequizando();
                    c.setId(rs.getInt("id"));
                    c.setNome(rs.getString("nome"));
                    c.setDataNascimento(rs.getDate("data_nascimento").toLocalDate());
                    c.setNomeResponsavel(rs.getString("nome_responsavel"));
                    c.setTelefoneResponsavel(rs.getString("telefone_responsavel"));
                    return c;
                },
                id
        );
    }
    @Override
    public List<Catequizando> buscarTodos() throws SQLException{
        return jdbc.query(
                "SELECT * FROM catequizando",
                (rs, rowNum) -> {
                    Catequizando c = new Catequizando();
                    c.setId(rs.getInt("id"));
                    c.setNome(rs.getString("nome"));
                    c.setDataNascimento(rs.getDate("data_nascimento").toLocalDate());
                    c.setNomeResponsavel(rs.getString("nome_responsavel"));
                    c.setTelefoneResponsavel(rs.getString("telefone_responsavel"));
                    return c;
                }
        );
    }
}