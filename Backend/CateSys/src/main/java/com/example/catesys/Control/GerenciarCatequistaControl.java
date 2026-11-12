
package com.example.catesys.Control;

import com.example.catesys.Model.Catequista;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class GerenciarCatequistaControl {

    private final Catequista model = new Catequista();

    public boolean cadastrar(Catequista catequista) throws SQLException {
        validarCatequista(catequista);

        Conexao conexao = Conexao.getInstancia();

        try {
            Connection conn = conexao.getConnection();

            verificarCpfDuplicado(conn, catequista);

            boolean gravou = catequista.gravar(conn);

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

            verificarCpfDuplicado(conn, catequista);

            boolean alterou = catequista.alterar(conn);

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
            boolean excluiu = model.excluir(
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
            return model.buscarPorId(
                    conexao.getConnection(), id
            );

        } finally {
            conexao.rollback();
        }
    }

    public List<Catequista> buscarTodos() throws SQLException {
        Conexao conexao = Conexao.getInstancia();

        try {
            return model.buscarTodos(
                    conexao.getConnection()
            );

        } finally {
            conexao.rollback();
        }
    }

    public List<Catequista> buscarPorNome(String nome)
            throws SQLException {

        if (nome == null) {
            throw new IllegalArgumentException("Nome obrigatorio");
        }

        Conexao conexao = Conexao.getInstancia();

        try {
            return model.buscarPorNome(
                    conexao.getConnection(), nome
            );

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
            return model.buscarPorCpf(
                    conexao.getConnection(), cpf
            );

        } finally {
            conexao.rollback();
        }
    }

    public List<Catequista> buscarPorCpfParcial(String cpf)
            throws SQLException {

        if (cpf == null || cpf.replaceAll("\\D", "").isBlank()) {
            throw new IllegalArgumentException(
                    "Informe ao menos um numero do CPF"
            );
        }

        Conexao conexao = Conexao.getInstancia();

        try {
            return model.buscarPorCpfParcial(
                    conexao.getConnection(), cpf
            );

        } finally {
            conexao.rollback();
        }
    }

    private void validarCatequista(Catequista catequista) {

        if (catequista == null) {
            throw new IllegalArgumentException(
                    "Catequista obrigatorio"
            );
        }

        catequista.validarDados();
    }

    private void verificarCpfDuplicado(
            Connection conn,
            Catequista catequista
    ) throws SQLException {

        Catequista existente = model.buscarPorCpf(
                conn, catequista.getCpf()
        );

        if (existente != null &&
                existente.getId() != catequista.getId()) {

            throw new IllegalArgumentException(
                    "Ja existe um catequista cadastrado com esse CPF"
            );
        }
    }
}
