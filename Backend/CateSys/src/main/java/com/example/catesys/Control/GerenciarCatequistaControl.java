
package com.example.catesys.Control;

import com.example.catesys.Model.Catequista;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class GerenciarCatequistaControl {

    private final Catequista model = new Catequista();

    // CADASTRAR CATEQUISTA

    public boolean cadastrar(Catequista catequista)
            throws SQLException {

        validarCatequista(catequista);

        Conexao conexao = Conexao.getInstancia();

        synchronized (conexao) {

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
                fazerRollback(conexao, e);
                throw e;
            }
        }
    }

    // ALTERAR CATEQUISTA

    public boolean alterar(Catequista catequista)
            throws SQLException {

        validarCatequista(catequista);

        if (catequista.getId() <= 0) {
            throw new IllegalArgumentException("ID invalido");
        }

        Conexao conexao = Conexao.getInstancia();

        synchronized (conexao) {

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
                fazerRollback(conexao, e);
                throw e;
            }
        }
    }

    // EXCLUIR CATEQUISTA

    public boolean excluir(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("ID invalido");
        }

        Conexao conexao = Conexao.getInstancia();

        synchronized (conexao) {

            try {
                boolean excluiu = model.excluir(
                        conexao.getConnection(),
                        id
                );

                if (excluiu) {
                    conexao.commit();
                } else {
                    conexao.rollback();
                }

                return excluiu;

            } catch (SQLException | RuntimeException e) {
                fazerRollback(conexao, e);
                throw e;
            }
        }
    }

    // CONSULTAR CATEQUISTA POR ID

    public Catequista buscarPorId(int id)
            throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("ID invalido");
        }

        Conexao conexao = Conexao.getInstancia();

        synchronized (conexao) {

            try {
                return model.buscarPorId(
                        conexao.getConnection(),
                        id
                );

            } finally {
                conexao.rollback();
            }
        }
    }

    // LISTAR TODOS OS CATEQUISTAS

    public List<Catequista> buscarTodos()
            throws SQLException {

        Conexao conexao = Conexao.getInstancia();

        synchronized (conexao) {

            try {
                return model.buscarTodos(
                        conexao.getConnection()
                );

            } finally {
                conexao.rollback();
            }
        }
    }

    // BUSCAR CATEQUISTAS POR NOME

    public List<Catequista> buscarPorNome(String nome)
            throws SQLException {

        if (nome == null) {
            throw new IllegalArgumentException(
                    "Nome obrigatorio"
            );
        }

        Conexao conexao = Conexao.getInstancia();

        synchronized (conexao) {

            try {
                return model.buscarPorNome(
                        conexao.getConnection(),
                        nome
                );

            } finally {
                conexao.rollback();
            }
        }
    }

    // BUSCAR CATEQUISTA POR CPF EXATO

    public Catequista buscarPorCpf(String cpf)
            throws SQLException {

        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException(
                    "CPF obrigatorio"
            );
        }

        Conexao conexao = Conexao.getInstancia();

        synchronized (conexao) {

            try {
                return model.buscarPorCpf(
                        conexao.getConnection(),
                        cpf
                );

            } finally {
                conexao.rollback();
            }
        }
    }

    // BUSCAR CATEQUISTAS POR CPF PARCIAL

    public List<Catequista> buscarPorCpfParcial(String cpf)
            throws SQLException {

        if (cpf == null ||
                cpf.replaceAll("\\D", "").isBlank()) {

            throw new IllegalArgumentException(
                    "Informe ao menos um numero do CPF"
            );
        }

        Conexao conexao = Conexao.getInstancia();

        synchronized (conexao) {

            try {
                return model.buscarPorCpfParcial(
                        conexao.getConnection(),
                        cpf
                );

            } finally {
                conexao.rollback();
            }
        }
    }

    // VALIDAR DADOS DO CATEQUISTA

    private void validarCatequista(
            Catequista catequista
    ) {

        if (catequista == null) {
            throw new IllegalArgumentException(
                    "Catequista obrigatorio"
            );
        }

        catequista.validarDados();
    }

    // VERIFICAR CPF DUPLICADO

    private void verificarCpfDuplicado(
            Connection conn,
            Catequista catequista
    ) throws SQLException {

        Catequista existente = model.buscarPorCpf(
                conn,
                catequista.getCpf()
        );

        if (existente != null &&
                existente.getId() != catequista.getId()) {

            throw new IllegalArgumentException(
                    "Ja existe um catequista cadastrado com esse CPF"
            );
        }
    }

    // DESFAZER TRANSACAO EM CASO DE ERRO

    private void fazerRollback(
            Conexao conexao,
            Exception erroOriginal
    ) {

        try {
            conexao.rollback();

        } catch (SQLException erroRollback) {
            erroOriginal.addSuppressed(erroRollback);
        }
    }
}
