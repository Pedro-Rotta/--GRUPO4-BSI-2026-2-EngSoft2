
package com.example.catesys.Model;

import com.example.catesys.DAO.TurmaCatequistaDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class TurmaCatequista {

    private int catequistaId;
    private int turmaId;

    public TurmaCatequista() {
    }

    public TurmaCatequista(int catequistaId, int turmaId) {
        this.catequistaId = catequistaId;
        this.turmaId = turmaId;
    }


    public List<TurmaCatequista> buscarPorTurma(
            Connection conn,
            int turmaId
    ) throws SQLException {

        TurmaCatequistaDAO dao =
                new TurmaCatequistaDAO(conn);

        return dao.buscarPorTurma(turmaId);
    }


    public boolean catequistaExiste(
            Connection conn,
            int catequistaId
    ) throws SQLException {

        TurmaCatequistaDAO dao =
                new TurmaCatequistaDAO(conn);

        return dao.catequistaExiste(catequistaId);
    }

    // VERIFICAR SE O VINCULO JA EXISTE

    public boolean existeVinculo(
            Connection conn,
            int turmaId,
            int catequistaId
    ) throws SQLException {

        TurmaCatequistaDAO dao =
                new TurmaCatequistaDAO(conn);

        return dao.existeVinculo(
                turmaId,
                catequistaId
        );
    }


    public boolean possuiConflitoHorario(
            Connection conn,
            int turmaId,
            int novoCatequistaId
    ) throws SQLException {

        TurmaCatequistaDAO dao =
                new TurmaCatequistaDAO(conn);

        return dao.possuiConflitoHorario(
                turmaId,
                novoCatequistaId
        );
    }

    public boolean trocarCatequista(
            Connection conn,
            int turmaId,
            int catequistaAtualId,
            int novoCatequistaId
    ) throws SQLException {

        TurmaCatequistaDAO dao =
                new TurmaCatequistaDAO(conn);

        return dao.trocarCatequista(
                turmaId,
                catequistaAtualId,
                novoCatequistaId
        );
    }

    public boolean gravar(Connection conn)
            throws SQLException {

        TurmaCatequistaDAO dao =
                new TurmaCatequistaDAO(conn);

        return dao.gravar(this);
    }


    public int getCatequistaId() {
        return catequistaId;
    }

    public void setCatequistaId(int catequistaId) {
        this.catequistaId = catequistaId;
    }

    public int getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(int turmaId) {
        this.turmaId = turmaId;
    }
}