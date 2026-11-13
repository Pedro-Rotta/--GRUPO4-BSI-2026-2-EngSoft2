package com.example.catesys.DAO;

import com.example.catesys.Model.Parametrizacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ParametrizacaoDAO {

    // A tabela pode ter apenas 1 registro, id sempre 1
    private static final int ID_UNICO = 1;

    // Recebe conexão da Control
    private final Connection conn;

    public ParametrizacaoDAO(Connection conn) {
        this.conn = conn;
    }

    // Retorna null se a parametrização ainda não foi feita
    public Parametrizacao buscar() throws SQLException {
        String sql = "SELECT * FROM parametrizacao WHERE parametrizacao_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ID_UNICO);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Parametrizacao p = new Parametrizacao( //tirar daqui--------------------
                        rs.getString("nome"),
                        rs.getString("cnpj"),
                        rs.getString("responsavel"));
                p.setId(rs.getInt("parametrizacao_id"));
                p.setTelefone(rs.getString("telefone"));
                p.setEmail(rs.getString("email"));
                p.setSite(rs.getString("site"));
                p.setDataCriacao(rs.getDate("data_criacao"));
                p.setEndereco(rs.getString("endereco"));
                p.setBairro(rs.getString("bairro"));
                p.setCidade(rs.getString("cidade"));
                p.setCep(rs.getString("cep"));
                p.setUf(rs.getString("uf"));
                p.setLogotipoGrande(rs.getString("logotipo_grande"));
                p.setLogotipoPequeno(rs.getString("logotipo_pequeno"));
                return p;
            }
        }
    }

    public void gravar(Parametrizacao p) throws SQLException {
        String sql = "INSERT INTO parametrizacao (parametrizacao_id, nome, cnpj, responsavel, "
                + "telefone, email, site, data_criacao, endereco, bairro, cidade, cep, uf, "
                + "logotipo_grande, logotipo_pequeno) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            ps.setInt(i++, ID_UNICO);
            ps.setString(i++, p.getNome());
            ps.setString(i++, p.getCnpj());
            ps.setString(i++, p.getResponsavel());
            ps.setString(i++, vazioParaNull(p.getTelefone()));
            ps.setString(i++, vazioParaNull(p.getEmail()));
            ps.setString(i++, vazioParaNull(p.getSite()));
            // Usa data de hoje caso nao venha preenchida
            java.util.Date data = (p.getDataCriacao() != null) ? p.getDataCriacao() : new java.util.Date();
            ps.setDate(i++, new java.sql.Date(data.getTime()));
            ps.setString(i++, vazioParaNull(p.getEndereco()));
            ps.setString(i++, vazioParaNull(p.getBairro()));
            ps.setString(i++, vazioParaNull(p.getCidade()));
            ps.setString(i++, vazioParaNull(p.getCep()));
            ps.setString(i++, vazioParaNull(p.getUf()));
            ps.setString(i++, vazioParaNull(p.getLogotipoGrande()));
            ps.setString(i++, vazioParaNull(p.getLogotipoPequeno()));
            ps.executeUpdate();
        }
    }

    public void alterar(Parametrizacao p) throws SQLException {
        // Nao altera a data de criação
        String sql = "UPDATE parametrizacao SET nome = ?, cnpj = ?, responsavel = ?, "
                + "telefone = ?, email = ?, site = ?, endereco = ?, bairro = ?, cidade = ?, "
                + "cep = ?, uf = ?, logotipo_grande = ?, logotipo_pequeno = ? "
                + "WHERE parametrizacao_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, p.getNome());
            ps.setString(i++, p.getCnpj());
            ps.setString(i++, p.getResponsavel());
            ps.setString(i++, vazioParaNull(p.getTelefone()));
            ps.setString(i++, vazioParaNull(p.getEmail()));
            ps.setString(i++, vazioParaNull(p.getSite()));
            ps.setString(i++, vazioParaNull(p.getEndereco()));
            ps.setString(i++, vazioParaNull(p.getBairro()));
            ps.setString(i++, vazioParaNull(p.getCidade()));
            ps.setString(i++, vazioParaNull(p.getCep()));
            ps.setString(i++, vazioParaNull(p.getUf()));
            ps.setString(i++, vazioParaNull(p.getLogotipoGrande()));
            ps.setString(i++, vazioParaNull(p.getLogotipoPequeno()));
            ps.setInt(i++, ID_UNICO);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Parametrização não encontrada para alteração.");
            }
        }
    }

    // Campo opcional em branco vai como NULL no banco e não como texto vazio
    private String vazioParaNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }
}