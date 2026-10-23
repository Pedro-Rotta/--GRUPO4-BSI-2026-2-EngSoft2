package com.example.catesys.Model;

import java.util.Date;

public class FrequenciaFalta {
    private Date frequenciaData;
    private int turmaId;
    private int matriculaId;
    private String justificativaBonificacao;

    public FrequenciaFalta(Date frequenciaData, int turmaId, int matriculaId) {
        this.frequenciaData = frequenciaData;
        this.turmaId = turmaId;
        this.matriculaId = matriculaId;
    }

    public Date getFrequenciaData() {
        return frequenciaData;
    }

    public void setFrequenciaData(Date frequenciaData) {
        this.frequenciaData = frequenciaData;
    }

    public int getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(int turmaId) {
        this.turmaId = turmaId;
    }

    public int getMatriculaId() {
        return matriculaId;
    }

    public void setMatriculaId(int matriculaId) {
        this.matriculaId = matriculaId;
    }

    public String getJustificativaBonificacao() {
        return justificativaBonificacao;
    }

    public void setJustificativaBonificacao(String justificativaBonificacao) {
        this.justificativaBonificacao = justificativaBonificacao;
    }
}
