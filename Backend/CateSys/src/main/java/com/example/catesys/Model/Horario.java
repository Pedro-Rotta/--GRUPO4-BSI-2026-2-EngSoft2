package com.example.catesys.Model;

import com.example.catesys.Model.Enums.DiaSemana;

import java.sql.Time;

public class Horario {
    private int id;
    private DiaSemana diaSemana;
    private Time horaInicio;
    private Time horaFim;

    public Horario(DiaSemana diaSemana, Time horaInicio, Time horaFim) {
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public int getId() {
        return id;
    }

    public DiaSemana getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(DiaSemana diaSemana) {
        this.diaSemana = diaSemana;
    }

    public Time getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(Time horaInicio) {
        this.horaInicio = horaInicio;
    }

    public Time getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(Time horaFim) {
        this.horaFim = horaFim;
    }
}
