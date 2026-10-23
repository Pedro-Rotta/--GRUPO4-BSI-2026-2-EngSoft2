package com.example.catesys.Model;

import com.example.catesys.Model.Enums.Etapa;
import com.example.catesys.Model.Enums.StatusTurma;
import java.util.Date;
public class Turma {
    private int id;
    private Etapa etapa;
    private String anoOferta;
    private Date dataInicio;
    private Date dataFim;
    private int vagasTotais;
    private int vagasDisponiveis;
    private StatusTurma status;
    private int catequistaId;
    private int localHorarioid;
    private int localId;
    private int horarioId;

    public Turma(Etapa etapa, String anoOferta, Date dataInicio, Date dataFim, int vagasTotais, int vagasDisponiveis, StatusTurma status, int catequistaId, int localHorarioid, int localId, int horarioId) {
        this.etapa = etapa;
        this.anoOferta = anoOferta;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.vagasTotais = vagasTotais;
        this.vagasDisponiveis = vagasDisponiveis;
        this.status = status;
        this.catequistaId = catequistaId;
        this.localHorarioid = localHorarioid;
        this.localId = localId;
        this.horarioId = horarioId;
    }

    public int getId() {
        return id;
    }

    public Etapa getEtapa() {
        return etapa;
    }

    public void setEtapa(Etapa etapa) {
        this.etapa = etapa;
    }

    public String getAnoOferta() {
        return anoOferta;
    }

    public void setAnoOferta(String anoOferta) {
        this.anoOferta = anoOferta;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public int getVagasTotais() {
        return vagasTotais;
    }

    public void setVagasTotais(int vagasTotais) {
        this.vagasTotais = vagasTotais;
    }

    public int getVagasDisponiveis() {
        return vagasDisponiveis;
    }

    public void setVagasDisponiveis(int vagasDisponiveis) {
        this.vagasDisponiveis = vagasDisponiveis;
    }

    public StatusTurma getStatus() {
        return status;
    }

    public void setStatus(StatusTurma status) {
        this.status = status;
    }

    public int getCatequistaId() {
        return catequistaId;
    }

    public void setCatequistaId(int catequistaId) {
        this.catequistaId = catequistaId;
    }

    public int getLocalHorarioid() {
        return localHorarioid;
    }

    public void setLocalHorarioid(int localHorarioid) {
        this.localHorarioid = localHorarioid;
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
}
