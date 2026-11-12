package com.example.catesys.Model;

import java.util.Date;

public class Frequencia {
    private Date data;
    private int turmaId;

    public Frequencia(Date data, int turmaId) {
        this.data = data;
        this.turmaId = turmaId;
    }

    public static Frequencia reconstituir(Date data, int turmaId) {
        Frequencia s = new Frequencia (data, turmaId);
        return s;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public int getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(int turmaId) {
        this.turmaId = turmaId;
    }
}
