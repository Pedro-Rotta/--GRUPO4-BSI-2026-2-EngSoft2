
package com.example.catesys.DAO;

import com.example.catesys.Model.TurmaCatequista;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TurmaCatequistaDAO {

    private final Connection conexao;

    public TurmaCatequistaDAO(Connection conexao) {
        this.conexao = conexao;
    }

    // CONSULTAR CATEQUISTAS VINCULADOS A UMA TURMA

    public List<TurmaCatequista> buscarPorTurma(int turmaId)
            throws SQLException {

        String sql = """
                SELECT
                    catequista_catequista_id,
                    turma_turma_id
                FROM turma_catequista
                WHERE turma_turma_id = ?
                ORDER BY catequista_catequista_id
                """;

        List<TurmaCatequista> lista = new ArrayList<>();

        try (PreparedStatement ps =
                     conexao.prepareStatement(sql)) {

            ps.setInt(1, turmaId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    TurmaCatequista vinculo =
                            new TurmaCatequista(
                                    rs.getInt(
                                            "catequista_catequista_id"
                                    ),
                                    rs.getInt("turma_turma_id")
                            );

                    lista.add(vinculo);
                }
            }
        }

        return lista;
    }

    // VERIFICAR SE O CATEQUISTA EXISTE

    public boolean catequistaExiste(int catequistaId)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM catequista
                WHERE catequista_id = ?
                """;

        try (PreparedStatement ps =
                     conexao.prepareStatement(sql)) {

            ps.setInt(1, catequistaId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // VERIFICAR SE A ASSOCIACAO JA EXISTE

    public boolean existeVinculo(int turmaId, int catequistaId)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM turma_catequista
                WHERE turma_turma_id = ?
                  AND catequista_catequista_id = ?
                """;

        try (PreparedStatement ps =
                     conexao.prepareStatement(sql)) {

            ps.setInt(1, turmaId);
            ps.setInt(2, catequistaId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // VERIFICAR CONFLITO DE DIA E HORARIO

    public boolean possuiConflitoHorario(
            int turmaId,
            int novoCatequistaId
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM turma turma_nova
                JOIN horario horario_novo
                  ON horario_novo.horario_id =
                     turma_nova.horario_horario_id

                JOIN turma_catequista vinculo
                  ON vinculo.catequista_catequista_id = ?

                JOIN turma outra_turma
                  ON outra_turma.turma_id =
                     vinculo.turma_turma_id

                JOIN horario outro_horario
                  ON outro_horario.horario_id =
                     outra_turma.horario_horario_id

                WHERE turma_nova.turma_id = ?
                  AND outra_turma.turma_id <> turma_nova.turma_id

                  AND UPPER(TRIM(outro_horario.dia_semana)) =
                      UPPER(TRIM(horario_novo.dia_semana))

                  AND horario_novo.hora_inicio <
                      outro_horario.hora_fim

                  AND outro_horario.hora_inicio <
                      horario_novo.hora_fim

                  AND outra_turma.status IN (
                      'ABERTA', 'EM_ANDAMENTO'
                  )

                  AND (
                      outra_turma.data_inicio IS NULL
                      OR turma_nova.data_fim IS NULL
                      OR outra_turma.data_inicio <=
                         turma_nova.data_fim
                  )

                  AND (
                      turma_nova.data_inicio IS NULL
                      OR outra_turma.data_fim IS NULL
                      OR turma_nova.data_inicio <=
                         outra_turma.data_fim
                  )

                LIMIT 1
                """;

        try (PreparedStatement ps =
                     conexao.prepareStatement(sql)) {

            ps.setInt(1, novoCatequistaId);
            ps.setInt(2, turmaId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // SUBSTITUIR SOMENTE O VINCULO SELECIONADO

    public boolean trocarCatequista(
            int turmaId,
            int catequistaAtualId,
            int novoCatequistaId
    ) throws SQLException {

        String sql = """
                UPDATE turma_catequista
                SET catequista_catequista_id = ?
                WHERE turma_turma_id = ?
                  AND catequista_catequista_id = ?
                """;

        try (PreparedStatement ps =
                     conexao.prepareStatement(sql)) {

            ps.setInt(1, novoCatequistaId);
            ps.setInt(2, turmaId);
            ps.setInt(3, catequistaAtualId);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean gravar(TurmaCatequista vinculo)
            throws SQLException {

        String sql = """
            INSERT INTO turma_catequista (
                catequista_catequista_id,
                turma_turma_id
            )
            VALUES (?, ?)
            """;

        try (PreparedStatement ps =
                     conexao.prepareStatement(sql)) {

            ps.setInt(1, vinculo.getCatequistaId());
            ps.setInt(2, vinculo.getTurmaId());

            return ps.executeUpdate() == 1;
        }
    }

}
