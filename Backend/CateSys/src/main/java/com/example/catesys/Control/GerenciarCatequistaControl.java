
package com.example.catesys.Control;

import com.example.catesys.Control.Conexao;
import com.example.catesys.Model.Catequista;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class GerenciarCatequistaControl {

    public boolean cadastrar(Catequista catequista) throws SQLException {
        validarCatequista(catequista);

        Conexao conexao = Conexao.getInstancia();

        try {
            verificarCpfDuplicado(conexao.getConnection(), catequista);
            boolean gravou = catequista.gravar(conexao.getConnection());

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
            verificarCpfDuplicado(conexao.getConnection(), catequista);
            boolean alterou = catequista.alterar(conexao.getConnection());

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
            boolean excluiu = Catequista.excluir(
                    conexao.getConnection(), id
            );

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
            return Catequista.buscarPorId(
                    conexao.getConnection(), id
            );
        } finally {
            conexao.rollback();
        }
    }

    public List<Catequista> buscarTodos() throws SQLException {
        Conexao conexao = Conexao.getInstancia();

        try {
            return Catequista.buscarTodos(
                    conexao.getConnection()
            );
        } finally {
            conexao.rollback();
        }
    }

    public List<Catequista> buscarPorNome(String nome) throws SQLException {
        if (nome == null) {
            throw new IllegalArgumentException("Nome nao pode ser null");
        }

        Conexao conexao = Conexao.getInstancia();

        try {
            return Catequista.buscarPorNome(
                    conexao.getConnection(), nome
            );
        } finally {
            conexao.rollback();
        }
    }

    public Catequista buscarPorCpf(String cpf) throws SQLException {
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF obrigatorio");
        }

        Conexao conexao = Conexao.getInstancia();

        try {
            return Catequista.buscarPorCpf(
                    conexao.getConnection(), cpf
            );
        } finally {
            conexao.rollback();
        }
    }

    private void validarCatequista(Catequista c) {
        if (c == null) {
            throw new IllegalArgumentException("Catequista obrigatorio");
        }

        c.validarDados();
    }

    private void verificarCpfDuplicado(Connection conn, Catequista c)
            throws SQLException {

        Catequista existente = Catequista.buscarPorCpf(
                conn, c.getCpf()
        );

        if (existente != null && existente.getId() != c.getId()) {
            throw new IllegalArgumentException(
                    "Ja existe um catequista cadastrado com esse CPF"
            );
        }
    }


}