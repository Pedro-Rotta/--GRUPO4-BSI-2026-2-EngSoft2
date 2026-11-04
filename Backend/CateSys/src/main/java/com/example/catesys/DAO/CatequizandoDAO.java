package com.example.catesys.DAO;

import com.example.catesys.Model.Catequizando;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
public class CatequizandoDAO implements IDAO<Catequizando> {

    @Override
    public boolean gravar(Catequizando c) throws SQLException {
        String sql = "INSERT INTO catequizando (nome, data_nascimento, nome_responsavel, telefone_responsavel) VALUES (?, ?, ?, ?)";
        try (var ps = Conexao.getInstancia().getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            ps.setDate(2, new java.sql.Date(c.getDataNascimento().getTime()));
            ps.setString(3, c.getNomeResponsavel());
            ps.setString(4, c.getTelefoneResponsavel());
            return ps.executeUpdate() > 0;
        }
    }
    @Override
    public boolean alterar(Catequizando obj) throws SQLException{
        String sql = ""
        return true;
    }
    @Override
    public boolean excluir(int id) throws SQLException{
        return true;
    }
    @Override
    public Catequizando buscarPorId(int id) throws SQLException{
        return new Catequizando();
    }
    @Override
    public List<Catequizando> buscarTodos() throws SQLException{
        return new ArrayList<Catequizando>();
    }
}