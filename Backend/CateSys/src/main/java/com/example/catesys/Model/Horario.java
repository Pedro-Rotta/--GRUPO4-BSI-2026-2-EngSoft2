package com.example.catesys.Model;

import com.example.catesys.Control.Conexao;
import com.example.catesys.DAO.HorarioDAO;
import com.example.catesys.Model.Enums.DiaSemana;

import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

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

    public Horario() {
    }

    public static Horario reconstituir(int id, DiaSemana diaSemana, Time horaInicio, Time horaFim) {
        Horario horario = new Horario(diaSemana, horaInicio, horaFim);
        horario.id = id;
        return horario;
    }

    // Persistência: a Model instancia o DAO (Control → Model → DAO).

    public Horario buscarPorId(int id, Conexao conexao) throws SQLException {
        return new HorarioDAO(conexao).buscarPorId(id);
    }

    public List<Horario> buscarTodos(Conexao conexao) throws SQLException {
        return new HorarioDAO(conexao).buscarTodos();
    }

    // Horários em que o local (e o catequista da turma, se houver) está livre no período da turma.
    public List<Horario> buscarDisponiveis(int localId, Turma turma, Conexao conexao) throws SQLException {
        return new HorarioDAO(conexao).buscarDisponiveis(localId, turma);
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
