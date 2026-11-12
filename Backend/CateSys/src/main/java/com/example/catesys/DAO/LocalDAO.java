package com.example.catesys.DAO;

import com.example.catesys.Control.Conexao;
import com.example.catesys.Model.Local;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Tabela "sala". Só consulta: o cadastro de locais (RF_B4) é outro caso de uso,
// por isso não implementa IDAO (SOLID-I).
public class LocalDAO {
    private final Conexao conexao;

    public LocalDAO(Conexao conexao) {
        this.conexao = conexao;
    }

    public Local buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM sala WHERE local_id = ?";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return lerLocal(rs);
            }
        }
        return null;
    }

    public List<Local> buscarTodos() throws SQLException {
        String sql = "SELECT * FROM sala ORDER BY nome";
        List<Local> lista = new ArrayList<>();
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                lista.add(lerLocal(rs));
        }
        return lista;
    }

    private Local lerLocal(ResultSet rs) throws SQLException {
        return Local.reconstituir(rs.getInt("local_id"), rs.getString("nome"), rs.getInt("capacidade"));
    }
}
