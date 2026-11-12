package com.example.catesys.DAO;

import com.example.catesys.Control.Conexao;
import com.example.catesys.Model.Enums.DiaSemana;
import com.example.catesys.Model.Horario;
import com.example.catesys.Model.Turma;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Só consulta (horários padrão já cadastrados), por isso não implementa IDAO (SOLID-I).
public class HorarioDAO {
    private final Conexao conexao;

    public HorarioDAO(Conexao conexao) {
        this.conexao = conexao;
    }

    // Ordena domingo..sábado e depois pela hora, independente de como o dia foi gravado.
    private static final String ORDEM = """
             ORDER BY CASE LEFT(UPPER(TRANSLATE(h.dia_semana, 'áàâãéêíóôõúçÁÀÂÃÉÊÍÓÔÕÚÇ', 'aaaaeeiooouCAAAAEEIOOOUC')), 3)
                 WHEN 'DOM' THEN 0 WHEN 'SEG' THEN 1 WHEN 'TER' THEN 2 WHEN 'QUA' THEN 3
                 WHEN 'QUI' THEN 4 WHEN 'SEX' THEN 5 ELSE 6 END, h.hora_inicio
            """;

    public Horario buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM horario h WHERE h.horario_id = ?";
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return lerHorario(rs);
            }
        }
        return null;
    }

    public List<Horario> buscarTodos() throws SQLException {
        return listar("SELECT * FROM horario h" + ORDEM);
    }

    // Horários livres para a turma no local informado: sem outra turma ativa no mesmo local
    // e, se a turma já tem catequista, sem outra turma ativa desse catequista,
    // em dia/horário e período sobrepostos.
    public List<Horario> buscarDisponiveis(int localId, Turma turma) throws SQLException {
        String ocupado = """
                SELECT 1 FROM turma t
                JOIN horario th ON th.horario_id = t.horario_horario_id
                WHERE t.turma_id <> ?
                  AND UPPER(t.status) NOT IN ('ENCERRADA', 'CANCELADA')
                  AND th.dia_semana = h.dia_semana
                  AND th.hora_inicio < h.hora_fim AND h.hora_inicio < th.hora_fim
                  AND COALESCE(t.data_inicio, '-infinity'::date) <= COALESCE(CAST(? AS date), 'infinity'::date)
                  AND COALESCE(CAST(? AS date), '-infinity'::date) <= COALESCE(t.data_fim, 'infinity'::date)
                """;
        String sql = "SELECT * FROM horario h"
                + " WHERE NOT EXISTS (" + ocupado + " AND t.sala_local_id = ?)"
                + " AND (? = 0 OR NOT EXISTS (" + ocupado + """
                 AND EXISTS (SELECT 1 FROM turma_catequista tc
                             WHERE tc.turma_turma_id = t.turma_id AND tc.catequista_catequista_id = ?)))
                """ + ORDEM;
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            preencherPeriodo(ps, 1, turma);
            ps.setInt(4, localId);
            ps.setInt(5, turma.getCatequistaId());
            preencherPeriodo(ps, 6, turma);
            ps.setInt(9, turma.getCatequistaId());
            return lerLista(ps);
        }
    }

    // id da turma (para não conflitar consigo mesma), data fim e data início.
    private void preencherPeriodo(PreparedStatement ps, int inicio, Turma turma) throws SQLException {
        ps.setInt(inicio, turma.getId());
        definirData(ps, inicio + 1, turma.getDataFim());
        definirData(ps, inicio + 2, turma.getDataInicio());
    }

    private List<Horario> listar(String sql) throws SQLException {
        try (PreparedStatement ps = conexao.getConnection().prepareStatement(sql)) {
            return lerLista(ps);
        }
    }

    private List<Horario> lerLista(PreparedStatement ps) throws SQLException {
        List<Horario> lista = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                lista.add(lerHorario(rs));
        }
        return lista;
    }

    private Horario lerHorario(ResultSet rs) throws SQLException {
        return Horario.reconstituir(
                rs.getInt("horario_id"),
                lerDiaSemana(rs.getString("dia_semana")),
                rs.getTime("hora_inicio"),
                rs.getTime("hora_fim"));
    }

    // O banco guarda o dia como texto ("Sábado", "SABADO", "Segunda-feira"...): converte para o enum.
    private DiaSemana lerDiaSemana(String valor) {
        if (valor == null || valor.isBlank())
            return null;
        String semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String chave = semAcento.trim().toUpperCase().split("[-\\s]")[0];
        try {
            return DiaSemana.valueOf(chave);
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
