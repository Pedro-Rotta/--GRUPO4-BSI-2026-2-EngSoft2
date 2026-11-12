
package com.example.catesys.Control;

import com.example.catesys.Model.Turma;
import com.example.catesys.Model.TurmaCatequista;

import java.sql.Connection;
import java.sql.SQLException;

public class TrocarCatequistaTurmaControl {

    private final Turma modelTurma = new Turma();

    private final TurmaCatequista modelVinculo =
            new TurmaCatequista();

    public boolean trocarCatequista(
            int turmaId,
            int catequistaAtualId,
            int novoCatequistaId
    ) throws SQLException {

        // VALIDAR IDENTIFICADORES

        if (turmaId <= 0) {
            throw new IllegalArgumentException(
                    "ID da turma invalido");
        }

        if (catequistaAtualId <= 0) {
            throw new IllegalArgumentException(
                    "Catequista atual invalido");
        }

        if (novoCatequistaId <= 0) {
            throw new IllegalArgumentException(
                    "Novo catequista invalido");
        }

        if (catequistaAtualId == novoCatequistaId) {
            throw new IllegalArgumentException(
                    "O novo catequista deve ser diferente do atual");
        }

        // CONEXAO SINGLETON

        Conexao conexao = Conexao.getInstancia();

        // PROTEGER A TRANSACAO COMPLETA

        synchronized (conexao) {

            try {

                Connection conn = conexao.getConnection();

                // VERIFICAR TURMA

                Turma turma = modelTurma.buscarPorId(
                        conn,
                        turmaId
                );

                if (turma == null) {
                    throw new IllegalArgumentException(
                            "Turma nao encontrada");
                }

                // VERIFICAR CATEQUISTA ATUAL

                boolean vinculoAtual =
                        modelVinculo.existeVinculo(
                                conn,
                                turmaId,
                                catequistaAtualId
                        );

                if (!vinculoAtual) {
                    throw new IllegalArgumentException(
                            "O catequista atual nao pertence a esta turma");
                }

                // VERIFICAR NOVO CATEQUISTA

                boolean novoExiste =
                        modelVinculo.catequistaExiste(
                                conn,
                                novoCatequistaId
                        );

                if (!novoExiste) {
                    throw new IllegalArgumentException(
                            "Novo catequista nao encontrado");
                }

                // IMPEDIR VINCULO DUPLICADO

                boolean jaVinculado =
                        modelVinculo.existeVinculo(
                                conn,
                                turmaId,
                                novoCatequistaId
                        );

                if (jaVinculado) {
                    throw new IllegalArgumentException(
                            "O novo catequista ja pertence a esta turma");
                }

                // VERIFICAR CONFLITO DE HORARIO

                boolean conflito =
                        modelVinculo.possuiConflitoHorario(
                                conn,
                                turmaId,
                                novoCatequistaId
                        );

                if (conflito) {
                    throw new IllegalArgumentException(
                            "O catequista possui conflito de horario com outra turma");
                }

                // EXECUTAR A TROCA

                boolean alterou =
                        modelVinculo.trocarCatequista(
                                conn,
                                turmaId,
                                catequistaAtualId,
                                novoCatequistaId
                        );

                if (!alterou) {
                    conexao.rollback();
                    return false;
                }

                // CONFIRMAR TRANSACAO

                conexao.commit();

                return true;

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
}
