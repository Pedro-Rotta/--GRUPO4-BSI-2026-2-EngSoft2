package com.example.catesys.DAO;

import com.example.catesys.Control.Conexao;
import com.example.catesys.Model.Enums.Etapa;
import com.example.catesys.Model.Enums.StatusTurma;
import com.example.catesys.Model.Turma;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Não implementa IDAO: os casos de uso desta parte (RF_F5 e RF_F8) não excluem turma,
// e a exclusão apagaria matrículas em cascata. Fica só com o que é usado (SOLID-I).
public class TurmaDAO {
    private final Conexao conexao;

    public TurmaDAO(Conexao conexao) {
        this.conexao = conexao;
    }

    // Grava a turma e o vínculo com o catequista (turma_catequista) na mesma transação.
    public boolean gravar(Turma turma) throws SQLException {
        String sql = """
                INSERT INTO turma (etapa, ano_oferta, data_inicio, data_fim, vagas_totais,
                vagas_disponiveis, status, sala_local_id, horario_horario_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, turma.getEtapa().name());
            ps.setString(2, turma.getAnoOferta());
            definirData(ps, 3, turma.getDataInicio());
            definirData(ps, 4, turma.getDataFim());
            ps.setInt(5, turma.getVagasTotais());
            ps.setInt(6, turma.getVagasDisponiveis());
            ps.setString(7, turma.getStatus().name());
            ps.setInt(8, turma.getLocalId());
            ps.setInt(9, turma.getHorarioId());
            if (ps.executeUpdate() == 0)
                return false;
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next())
                    turma.setId(chaves.getInt(1));
            }
        }

        String sqlCatequista = "INSERT INTO turma_catequista (catequista_catequista_id, turma_turma_id) VALUES (?, ?)";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sqlCatequista)) {
            ps.setInt(1, turma.getCatequistaId());
            ps.setInt(2, turma.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // RF_F8: troca local e horário da turma.
    public boolean alterarLocal(Turma turma) throws SQLException {
        String sql = "UPDATE turma SET sala_local_id = ?, horario_horario_id = ? WHERE turma_id = ?";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, turma.getLocalId());
            ps.setInt(2, turma.getHorarioId());
            ps.setInt(3, turma.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public Turma buscarPorId(int id) throws SQLException {
        String sql = SELECT_TURMA + " WHERE t.turma_id = ?";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return lerTurma(rs);
            }
        }
        return null;
    }

    public List<Turma> buscarTodas() throws SQLException {
        String sql = SELECT_TURMA + " ORDER BY t.ano_oferta DESC, t.etapa, t.turma_id";
        List<Turma> lista = new ArrayList<>();
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                lista.add(lerTurma(rs));
        }
        return lista;
    }

    public boolean existeConflitoLocal(Turma turma) throws SQLException {
        String sql = "SELECT 1 " + SOBREPOSICAO + " AND t.sala_local_id = ? LIMIT 1";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            preencherSobreposicao(ps, turma);
            ps.setInt(5, turma.getLocalId());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existeConflitoCatequista(Turma turma) throws SQLException {
        String sql = "SELECT 1 " + SOBREPOSICAO + """
                 AND EXISTS (SELECT 1 FROM turma_catequista tc
                             WHERE tc.turma_turma_id = t.turma_id AND tc.catequista_catequista_id = ?)
                LIMIT 1
                """;
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            preencherSobreposicao(ps, turma);
            ps.setInt(5, turma.getCatequistaId());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Catequista: a turma pode ter mais de um em turma_catequista; aqui vale o primeiro.
    private static final String SELECT_TURMA = """
            SELECT t.*, (SELECT MIN(tc.catequista_catequista_id) FROM turma_catequista tc
                         WHERE tc.turma_turma_id = t.turma_id) AS catequista_id
            FROM turma t
            """;

    // Outra turma ativa (t), no mesmo dia da semana, com horário e período sobrepostos aos da turma informada.
    // Parâmetros: 1 horário da turma, 2 id da turma (0 se nova), 3 data fim, 4 data início.
    private static final String SOBREPOSICAO = """
            FROM turma t
            JOIN horario th ON th.horario_id = t.horario_horario_id
            JOIN horario hn ON hn.horario_id = ?
            WHERE t.turma_id <> ?
              AND UPPER(t.status) NOT IN ('ENCERRADA', 'CANCELADA')
              AND th.dia_semana = hn.dia_semana
              AND th.hora_inicio < hn.hora_fim AND hn.hora_inicio < th.hora_fim
              AND COALESCE(t.data_inicio, '-infinity'::date) <= COALESCE(CAST(? AS date), 'infinity'::date)
              AND COALESCE(CAST(? AS date), '-infinity'::date) <= COALESCE(t.data_fim, 'infinity'::date)
            """;

    private void preencherSobreposicao(PreparedStatement ps, Turma turma) throws SQLException {
        ps.setInt(1, turma.getHorarioId());
        ps.setInt(2, turma.getId());
        definirData(ps, 3, turma.getDataFim());
        definirData(ps, 4, turma.getDataInicio());
    }

    private Turma lerTurma(ResultSet rs) throws SQLException {
        return Turma.reconstituir(
                rs.getInt("turma_id"),
                lerEnum(Etapa.class, rs.getString("etapa")),
                rs.getString("ano_oferta"),
                rs.getDate("data_inicio"),
                rs.getDate("data_fim"),
                rs.getInt("vagas_totais"),
                rs.getInt("vagas_disponiveis"),
                lerEnum(StatusTurma.class, rs.getString("status")),
                rs.getInt("catequista_id"),
                rs.getInt("sala_local_id"),
                rs.getInt("horario_horario_id"));
    }

    // Aceita "OFERTADA" e o padrão antigo do banco "Ofertada"; valor desconhecido vira null.
    private <E extends Enum<E>> E lerEnum(Class<E> tipo, String valor) {
        if (valor == null || valor.isBlank())
            return null;
        try {
            return Enum.valueOf(tipo, valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void definirData(PreparedStatement ps, int posicao, Date data) throws SQLException {
        if (data != null)
            ps.setDate(posicao, new java.sql.Date(data.getTime()));
        else
            ps.setNull(posicao, Types.DATE);
    }
}
