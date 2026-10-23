package com.example.catesys.Model;

import com.example.catesys.Model.Enums.StatusMatricula;

import java.util.Date;

public class Matricula {
    private int id;
    private Date dataMatricula;
    private StatusMatricula status;
    private int catequizandoId;
    private int turmaId;
    private int secretariaId;
    private String motivoDesistencia;
    private Date dataDesistencia;

    public Matricula(Date dataMatricula, StatusMatricula status, int catequizandoId, int turmaId, int secretariaId) {
        this.dataMatricula = dataMatricula;
        this.status = status;
        this.catequizandoId = catequizandoId;
        this.turmaId = turmaId;
        this.secretariaId = secretariaId;
    }

    public int getId() {
        return id;
    }

    public Date getDataMatricula() {
        return dataMatricula;
    }

    public void setDataMatricula(Date dataMatricula) {
        this.dataMatricula = dataMatricula;
    }

    public StatusMatricula getStatus() {
        return status;
    }

    public void setStatus(StatusMatricula status) {
        this.status = status;
    }

    public int getCatequizandoId() {
        return catequizandoId;
    }

    public void setCatequizandoId(int catequizandoId) {
        this.catequizandoId = catequizandoId;
    }

    public int getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(int turmaId) {
        this.turmaId = turmaId;
    }

    public int getSecretariaId() {
        return secretariaId;
    }

    public void setSecretariaId(int secretariaId) {
        this.secretariaId = secretariaId;
    }

    public String getMotivoDesistencia() {
        return motivoDesistencia;
    }

    public void setMotivoDesistencia(String motivoDesistencia) {
        this.motivoDesistencia = motivoDesistencia;
    }

    public Date getDataDesistencia() {
        return dataDesistencia;
    }

    public void setDataDesistencia(Date dataDesistencia) {
        this.dataDesistencia = dataDesistencia;
    }
}
