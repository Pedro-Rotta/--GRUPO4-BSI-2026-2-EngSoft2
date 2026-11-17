package com.example.catesys.Model;

import com.example.catesys.Control.Conexao;
import com.example.catesys.DAO.TurmaCatequistaDAO;

import java.sql.SQLException;
import java.util.List;

// Vínculo entre catequista e turma (tabela turma_catequista).
public class TurmaCatequista {
    private int catequistaId;
    private int turmaId;

    // RF_F4: a Control instancia o vínculo atual (turma + catequista que sai).
    public TurmaCatequista(int catequistaId, int turmaId) {
        this.catequistaId = catequistaId;
        this.turmaId = turmaId;
    }

    // Consultas: a Control instancia uma Model vazia e chama a busca.
    public TurmaCatequista() {
    }

    public List<String> validar() {
        return turmaId > 0 && catequistaId > 0 ? List.of() : List.of("Turma e catequista são obrigatórios");
    }

    // Persistência: a Model instancia o DAO com a Conexao recebida da Control.

    public boolean existe(Conexao conexao) throws SQLException {
        return new TurmaCatequistaDAO(conexao).existeVinculo(this);
    }

    // Troca só este vínculo: o catequista atual sai e o novo entra na mesma turma.
    public boolean trocarPor(int novoCatequistaId, Conexao conexao) throws SQLException {
        return new TurmaCatequistaDAO(conexao).trocarCatequista(this, novoCatequistaId);
    }

    public List<TurmaCatequista> buscarPorTurma(int turmaId, Conexao conexao) throws SQLException {
        return new TurmaCatequistaDAO(conexao).buscarPorTurma(turmaId);
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
