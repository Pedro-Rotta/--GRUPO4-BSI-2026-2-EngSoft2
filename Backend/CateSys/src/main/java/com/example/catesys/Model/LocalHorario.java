package com.example.catesys.Model;

public class LocalHorario {
    private int id;
    private int localId;
    private int horarioId;
    private boolean ocupado;    //CONFERIR O TIPO

    public LocalHorario(int localId, int horarioId, boolean ocupado) {
        this.localId = localId;
        this.horarioId = horarioId;
        this.ocupado = ocupado;
    }

    public int getId() {
        return id;
    }

    public int getLocalId() {
        return localId;
    }

    public void setLocalId(int localId) {
        this.localId = localId;
    }

    public int getHorarioId() {
        return horarioId;
    }

    public void setHorarioId(int horarioId) {
        this.horarioId = horarioId;
    }

    public boolean isOcupado() {
        return ocupado;
    }

    public void setOcupado(boolean ocupado) {
        this.ocupado = ocupado;
    }
}
