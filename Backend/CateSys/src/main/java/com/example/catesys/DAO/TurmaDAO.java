
package com.example.catesys.DAO;

import com.example.catesys.Model.Turma;
import com.example.catesys.Model.Enums.Etapa;
import com.example.catesys.Model.Enums.StatusTurma;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TurmaDAO {

    private final Connection conexao;

    public TurmaDAO(Connection conexao) {
        this.conexao = conexao;
    }

    public List<Turma> buscarTodos() throws SQLException {

        String sql = """
                SELECT
                    turma_id,
                    etapa,
                    ano_oferta,
                    data_inicio,
                    data_fim,
                    vagas_totais,
                    vagas_disponiveis,
                    status,
                    local_horario_id,
                    sala_local_id,
                    horario_horario_id
                FROM turma
                ORDER BY turma_id
                """;

        List<Turma> lista = new ArrayList<>();

        try (PreparedStatement ps =
                     conexao.prepareStatement(sql);

             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(lerTurma(rs));
            }
        }

        return lista;
    }

    private Turma lerTurma(ResultSet rs)
            throws SQLException {

        Etapa etapa = converterEtapa(
                rs.getString("etapa")
        );

        StatusTurma status = converterStatus(
                rs.getString("status")
        );

        Turma turma = new Turma(
                etapa,
                rs.getString("ano_oferta"),
                rs.getDate("data_inicio"),
                rs.getDate("data_fim"),
                rs.getInt("vagas_totais"),
                rs.getInt("vagas_disponiveis"),
                status,
                0,
                rs.getInt("local_horario_id"),
                rs.getInt("sala_local_id"),
                rs.getInt("horario_horario_id")
        );

        turma.setId(rs.getInt("turma_id"));

        return turma;
    }

    private Etapa converterEtapa(String valor)
            throws SQLException {

        if (valor == null) {
            return null;
        }

        try {
            return Etapa.valueOf(normalizar(valor));
        } catch (IllegalArgumentException e) {
            throw new SQLException(
                    "Etapa de turma nao reconhecida: " + valor,
                    e
            );
        }
    }

    private StatusTurma converterStatus(String valor)
            throws SQLException {

        if (valor == null) {
            return null;
        }

        try {
            return StatusTurma.valueOf(normalizar(valor));
        } catch (IllegalArgumentException e) {
            throw new SQLException(
                    "Status de turma nao reconhecido: " + valor,
                    e
            );
        }
    }

    private String normalizar(String valor) {
        return valor.trim()
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_');
    }

    public Turma buscarPorId(int id) throws SQLException {

        String sql = """
            SELECT
                turma_id,
                etapa,
                ano_oferta,
                data_inicio,
                data_fim,
                vagas_totais,
                vagas_disponiveis,
                status,
                local_horario_id,
                sala_local_id,
                horario_horario_id
            FROM turma
            WHERE turma_id = ?
            """;

        try (PreparedStatement ps =
                     conexao.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return lerTurma(rs);
                }
            }
        }

        return null;
    }

    public int gravar(Turma turma) throws SQLException {

        // A tabela turma não tem ID automático.
        // O bloqueio evita que duas inserções simultâneas
        // desta aplicação escolham o mesmo ID.
        try (PreparedStatement lock = conexao.prepareStatement(
                "LOCK TABLE turma IN EXCLUSIVE MODE")) {
            lock.execute();
        }

        String sql = """
            INSERT INTO turma (
                turma_id,
                etapa,
                ano_oferta,
                data_inicio,
                data_fim,
                vagas_totais,
                vagas_disponiveis,
                status,
                local_horario_id,
                sala_local_id,
                horario_horario_id
            )
            SELECT
                COALESCE(MAX(turma_id), 0) + 1,
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
            FROM turma
            RETURNING turma_id
            """;

        try (PreparedStatement ps =
                     conexao.prepareStatement(sql)) {

            ps.setString(1, turma.getEtapa().name());
            ps.setString(2, turma.getAnoOferta());

            ps.setDate(3, new java.sql.Date(
                    turma.getDataInicio().getTime()));

            ps.setDate(4, new java.sql.Date(
                    turma.getDataFim().getTime()));

            ps.setInt(5, turma.getVagasTotais());
            ps.setInt(6, turma.getVagasDisponiveis());
            ps.setString(7, turma.getStatus().name());

            if (turma.getLocalHorarioid() > 0) {
                ps.setInt(8, turma.getLocalHorarioid());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }

            ps.setInt(9, turma.getLocalId());
            ps.setInt(10, turma.getHorarioId());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException(
                            "Não foi possível obter o ID da turma");
                }

                return rs.getInt("turma_id");
            }
        }
    }
}
