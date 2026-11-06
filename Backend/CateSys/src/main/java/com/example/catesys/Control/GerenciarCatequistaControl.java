
package com.example.catesys.Control;

import com.example.catesys.DAO.CatequistaDAO;
import com.example.catesys.Model.Catequista;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class GerenciarCatequistaControl {

    public boolean cadastrar(Catequista catequista) throws SQLException {
        validarCatequista(catequista);

        Conexao conexao = Conexao.getInstancia();

        try {
            Connection conn = conexao.getConnection();
            CatequistaDAO dao = new CatequistaDAO(conn);

            verificarCpfDuplicado(dao, catequista);

            boolean gravou = dao.gravar(catequista);

            if (gravou) {
                conexao.commit();
            } else {
                conexao.rollback();
            }

            return gravou;

        } catch (SQLException | RuntimeException e) {
            conexao.rollback();
            throw e;
        }
    }

    public boolean alterar(Catequista catequista) throws SQLException {
        validarCatequista(catequista);

        if (catequista.getId() <= 0) {
            throw new IllegalArgumentException("ID invalido");
        }

        Conexao conexao = Conexao.getInstancia();

        try {
            Connection conn = conexao.getConnection();
            CatequistaDAO dao = new CatequistaDAO(conn);

            verificarCpfDuplicado(dao, catequista);

            boolean alterou = dao.alterar(catequista);

            if (alterou) {
                conexao.commit();
            } else {
                conexao.rollback();
            }

            return alterou;

        } catch (SQLException | RuntimeException e) {
            conexao.rollback();
            throw e;
        }
    }

    public boolean excluir(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID invalido");
        }

        Conexao conexao = Conexao.getInstancia();

        try {
            CatequistaDAO dao =
                    new CatequistaDAO(conexao.getConnection());

            boolean excluiu = dao.excluir(id);

            if (excluiu) {
                conexao.commit();
            } else {
                conexao.rollback();
            }

            return excluiu;

        } catch (SQLException | RuntimeException e) {
            conexao.rollback();
            throw e;
        }
    }

    public Catequista buscarPorId(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID invalido");
        }

        Conexao conexao = Conexao.getInstancia();

        try {
            CatequistaDAO dao =
                    new CatequistaDAO(conexao.getConnection());

            return dao.buscarPorId(id);

        } finally {
            conexao.rollback();
        }
    }

    public List<Catequista> buscarTodos() throws SQLException {
        Conexao conexao = Conexao.getInstancia();

        try {
            CatequistaDAO dao =
                    new CatequistaDAO(conexao.getConnection());

            return dao.buscarTodos();

        } finally {
            conexao.rollback();
        }
    }

    public List<Catequista> buscarPorNome(String nome)
            throws SQLException {

        if (nome == null) {
            throw new IllegalArgumentException("Nome nao pode ser null");
        }

        Conexao conexao = Conexao.getInstancia();

        try {
            CatequistaDAO dao =
                    new CatequistaDAO(conexao.getConnection());

            return dao.buscarPorNome(nome);

        } finally {
            conexao.rollback();
        }
    }

    public Catequista buscarPorCpf(String cpf)
            throws SQLException {

        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF obrigatorio");
        }

        Conexao conexao = Conexao.getInstancia();

        try {
            CatequistaDAO dao =
                    new CatequistaDAO(conexao.getConnection());

            return dao.buscarPorCpf(cpf);

        } finally {
            conexao.rollback();
        }
    }

    private void validarCatequista(Catequista catequista) {
        if (catequista == null) {
            throw new IllegalArgumentException("Catequista obrigatorio");
        }

        catequista.validarDados();
    }

    private void verificarCpfDuplicado(
            CatequistaDAO dao,
            Catequista catequista
    ) throws SQLException {

        Catequista existente =
                dao.buscarPorCpf(catequista.getCpf());

        if (existente != null &&
                existente.getId() != catequista.getId()) {

            throw new IllegalArgumentException(
                    "Ja existe um catequista cadastrado com esse CPF"
            );
        }
    }
}
