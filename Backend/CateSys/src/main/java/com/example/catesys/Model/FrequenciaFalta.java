package com.example.catesys.Model;

import java.util.Date;

// Falta de uma matrícula em um encontro (tabela frequencia_faltas).
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

    public static FrequenciaFalta reconstituir(Date frequenciaData, int turmaId, int matriculaId,
                                               String justificativaBonificacao) {
        FrequenciaFalta f = new FrequenciaFalta(frequenciaData, turmaId, matriculaId);
        f.justificativaBonificacao = justificativaBonificacao;
        return f;
    }

    public Date getFrequenciaData() {
        return frequenciaData;
    }

    public int getTurmaId() {
        return turmaId;
    }

    public int getMatriculaId() {
        return matriculaId;
    }

    public String getJustificativaBonificacao() {
        return justificativaBonificacao;
    }

    public void setJustificativaBonificacao(String justificativaBonificacao) {
        this.justificativaBonificacao = justificativaBonificacao;
    }
}
