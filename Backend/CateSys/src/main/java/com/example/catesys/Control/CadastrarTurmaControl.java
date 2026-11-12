
package com.example.catesys.Control;

import com.example.catesys.Model.Turma;
import com.example.catesys.Model.TurmaCatequista;

import java.sql.Connection;
import java.sql.SQLException;

public class CadastrarTurmaControl {

    public int cadastrar(
            Turma turma,
            int catequistaId
    ) throws SQLException {

        validarDados(turma, catequistaId);

        Conexao conexao = Conexao.getInstancia();

        // Protege as operações desta Control
        // sobre a conexão compartilhada.
        synchronized (conexao) {

            try {
                Connection conn = conexao.getConnection();

                TurmaCatequista modelVinculo =
                        new TurmaCatequista();

                // VERIFICAR CATEQUISTA

                if (!modelVinculo.catequistaExiste(
                        conn, catequistaId)) {

                    throw new IllegalArgumentException(
                            "Catequista não encontrado");
                }

                // CADASTRAR A TURMA

                if (!turma.gravar(conn)) {
                    throw new SQLException(
                            "Não foi possível cadastrar a turma");
                }

                // VINCULAR O CATEQUISTA INICIAL

                TurmaCatequista vinculo =
                        new TurmaCatequista(
                                catequistaId,
                                turma.getId()
                        );

                if (!vinculo.gravar(conn)) {
                    throw new SQLException(
                            "Não foi possível vincular o catequista");
                }

                // CONFIRMAR AS DUAS OPERAÇÕES

                conexao.commit();

                return turma.getId();

            } catch (SQLException | RuntimeException e) {

                try {
                    conexao.rollback();
                } catch (SQLException erroRollback) {
                    e.addSuppressed(erroRollback);
                }

                throw e;
            }
        }
    }

    private void validarDados(
            Turma turma,
            int catequistaId
    ) {

        if (turma == null) {
            throw new IllegalArgumentException(
                    "Dados da turma obrigatórios");
        }

        if (turma.getEtapa() == null) {
            throw new IllegalArgumentException(
                    "Etapa obrigatória");
        }

        if (turma.getAnoOferta() == null ||
                !turma.getAnoOferta().matches("\\d{4}")) {

            throw new IllegalArgumentException(
                    "Informe um ano de oferta válido");
        }

        if (turma.getDataInicio() == null) {
            throw new IllegalArgumentException(
                    "Data de início obrigatória");
        }

        if (turma.getDataFim() == null) {
            throw new IllegalArgumentException(
                    "Data de término obrigatória");
        }

        if (turma.getDataFim().before(
                turma.getDataInicio())) {

            throw new IllegalArgumentException(
                    "A data de término não pode ser " +
                            "anterior à data de início");
        }

        if (turma.getVagasTotais() <= 0) {
            throw new IllegalArgumentException(
                    "Total de vagas deve ser maior que zero");
        }

        if (turma.getVagasDisponiveis() !=
                turma.getVagasTotais()) {

            throw new IllegalArgumentException(
                    "Uma turma nova deve iniciar " +
                            "com todas as vagas disponíveis");
        }

        if (turma.getStatus() == null) {
            throw new IllegalArgumentException(
                    "Status obrigatório");
        }

        if (turma.getLocalId() <= 0) {
            throw new IllegalArgumentException(
                    "ID da sala inválido");
        }

        if (turma.getHorarioId() <= 0) {
            throw new IllegalArgumentException(
                    "ID do horário inválido");
        }

        if (catequistaId <= 0) {
            throw new IllegalArgumentException(
                    "Catequista inválido");
        }
    }
}
