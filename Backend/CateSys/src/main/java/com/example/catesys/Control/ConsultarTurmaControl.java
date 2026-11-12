
package com.example.catesys.Control;

import com.example.catesys.Model.Turma;
import com.example.catesys.Model.TurmaCatequista;

import java.sql.SQLException;
import java.util.List;

public class ConsultarTurmaControl {

    private final Turma model = new Turma();

    private final TurmaCatequista modelVinculo =
            new TurmaCatequista();

    public record DetalhesTurma(
            Turma turma,
            List<TurmaCatequista> vinculos
    ) {}

    // LISTAR TODAS AS TURMAS

    public List<Turma> buscarTodos() throws SQLException {

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

    // CONSULTAR TURMA POR ID

    public DetalhesTurma buscarPorId(int id)
            throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID da turma invalido");
        }

        Conexao conexao = Conexao.getInstancia();

        synchronized (conexao) {

            try {

                Turma turma = model.buscarPorId(
                        conexao.getConnection(),
                        id
                );

                if (turma == null) {
                    return null;
                }

                List<TurmaCatequista> vinculos =
                        modelVinculo.buscarPorTurma(
                                conexao.getConnection(),
                                id
                        );

                return new DetalhesTurma(
                        turma,
                        vinculos
                );

            } finally {

                conexao.rollback();
            }
        }
    }
}
