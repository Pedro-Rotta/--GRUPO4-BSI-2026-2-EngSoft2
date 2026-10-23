package com.example.catesys.Model;

public class TurmaCatequista {
    private int catequistaId;
    private int turmaId;

    public TurmaCatequista(int catequistaId, int turmaId) {
        this.catequistaId = catequistaId;
        this.turmaId = turmaId;
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
