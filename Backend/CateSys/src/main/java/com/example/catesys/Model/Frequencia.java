package com.example.catesys.Model;

import com.example.catesys.Control.Conexao;
import com.example.catesys.DAO.FrequenciaDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

// RF_F9 - Encontro de uma turma em uma data, com as faltas dos catequizandos.
public class Frequencia {
    private Date data;
    private int turmaId;
    private List<FrequenciaFalta> faltas = new ArrayList<>();

    // A Control instancia o encontro com a data, a turma e as matrículas que faltaram.
    public Frequencia(Date data, int turmaId, List<Integer> matriculasAusentes) {
        this.data = data;
        this.turmaId = turmaId;
        if (matriculasAusentes != null)
            for (Integer matriculaId : new LinkedHashSet<>(matriculasAusentes)) // ignora repetidas
                if (matriculaId != null)
                    faltas.add(new FrequenciaFalta(data, turmaId, matriculaId));
    }

    public List<String> validar() {
        List<String> erros = new ArrayList<>();
        if (data == null)
            erros.add("Data do encontro é obrigatória");
        else if (data.after(new Date()))
            erros.add("Não é possível lançar presença em data futura");
        if (turmaId <= 0)
            erros.add("Turma é obrigatória");
        if (faltas.stream().anyMatch(f -> f.getMatriculaId() <= 0))
            erros.add("Matrícula inválida na lista de faltas");
        return erros;
    }

    // Todas as faltas precisam ser de matrículas ativas desta turma.
    public boolean faltasPertencemATurma(List<Integer> matriculasAtivas) {
        return faltas.stream().allMatch(f -> matriculasAtivas.contains(f.getMatriculaId()));
    }

    // Persistência: a Model instancia o DAO com a Conexao recebida da Control.

    public boolean jaLancada(Conexao conexao) throws SQLException {
        return new FrequenciaDAO(conexao).existe(this);
    }

    // Grava o encontro e as faltas; a Control faz commit ou rollback de tudo junto.
    public boolean gravar(Conexao conexao) throws SQLException {
        return new FrequenciaDAO(conexao).gravar(this);
    }

    public List<Integer> matriculasAtivasDaTurma(Conexao conexao) throws SQLException {
        return new FrequenciaDAO(conexao).matriculasAtivas(turmaId);
    }

    public Date getData() {
        return data;
    }

    public int getTurmaId() {
        return turmaId;
    }

    public List<FrequenciaFalta> getFaltas() {
        return faltas;
    }
}
