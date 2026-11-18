package com.example.catesys.Model;

import com.example.catesys.DAO.MatriculaDAO;
import com.example.catesys.Model.Enums.StatusMatricula;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Matricula {
    // Tamanho da coluna motivo_desistencia no banco
    private static final int MAX_MOTIVO = 45;

    private int id;
    private Date dataMatricula;
    private StatusMatricula status;
    private int catequizandoId;
    private int turmaId;
    private int secretariaId;
    private String motivoDesistencia;
    private Date dataDesistencia;

    // A Model é quem instancia o DAO
    private MatriculaDAO dao;

    public Matricula(Date dataMatricula, StatusMatricula status, int catequizandoId, int turmaId, int secretariaId) {
        this.dataMatricula = dataMatricula;
        this.status = status;
        this.catequizandoId = catequizandoId;
        this.turmaId = turmaId;
        this.secretariaId = secretariaId;
    }

    // Usado pela Control, ela entrega a conexão e a Model cria o DAO
    public Matricula(Connection conn) {
        this.dao = new MatriculaDAO(conn);
    }

    // ---------- F1: Matricular ----------

    // Confere as regras, grava a matrícula e atualiza as vagas da turma.
    public Map<String, String> matricular(int catequizandoId, int turmaId, int secretariaId) throws SQLException {
        Map<String, String> erros = new LinkedHashMap<>();
        if (catequizandoId <= 0) erros.put("catequizandoId", "Selecione o catequizando.");
        if (turmaId <= 0) erros.put("turmaId", "Selecione a turma.");
        if (!erros.isEmpty()) return erros;

        if (!dao.catequizandoExiste(catequizandoId)) {
            erros.put("catequizandoId", "Catequizando não encontrado.");
            return erros;
        }

        // Se ele já tem matrícula ativa, DAO preenche esta Model com ela e dá para dizer em qual turma
        if (dao.buscarAtivaDoCatequizando(this, catequizandoId)) {
            erros.put("catequizandoId", "Este catequizando já está matriculado na turma " + this.turmaId + ".");
            return erros;
        }

        Integer vagasTotais = dao.vagasTotaisDaTurma(turmaId);
        if (vagasTotais == null) {
            erros.put("turmaId", "Turma não encontrada.");
            return erros;
        }
        if (dao.contarAtivasDaTurma(turmaId) >= vagasTotais) {
            erros.put("turmaId", "Não há vagas disponíveis nesta turma.");
            return erros;
        }

        this.dataMatricula = new Date();
        this.status = StatusMatricula.ATIVA;
        this.catequizandoId = catequizandoId;
        this.turmaId = turmaId;
        this.secretariaId = secretariaId;
        dao.gravar(this);
        dao.atualizarVagas(turmaId);
        return erros;
    }

    // ---------- F2: Desmatricular ----------

    // Encerra a matrícula ativa do catequizando e devolve a vaga para a turma.
    public Map<String, String> desmatricular(int catequizandoId, String motivo) throws SQLException {
        Map<String, String> erros = new LinkedHashMap<>();
        if (catequizandoId <= 0) erros.put("catequizandoId", "Selecione o catequizando.");
        if (motivo != null && motivo.trim().length() > MAX_MOTIVO) {
            erros.put("motivo", "Máximo de " + MAX_MOTIVO + " caracteres.");
        }
        if (!erros.isEmpty()) return erros;

        if (!dao.catequizandoExiste(catequizandoId)) {
            erros.put("catequizandoId", "Catequizando não encontrado.");
            return erros;
        }

        // O DAO preenche esta Model com a matrícula ativa
        if (!dao.buscarAtivaDoCatequizando(this, catequizandoId)) {
            erros.put("catequizandoId", "Este catequizando não está matriculado em nenhuma turma.");
            return erros;
        }

        this.status = StatusMatricula.DESISTENTE;
        this.dataDesistencia = new Date();
        this.motivoDesistencia = (motivo == null) ? null : motivo.trim();
        dao.encerrar(this);
        dao.atualizarVagas(this.turmaId);
        return erros;
    }

    // ---------- Listas para a tela ----------

    public List<Map<String, Object>> listarCatequizandosSemMatriculaAtiva() throws SQLException {
        return dao.listarCatequizandosSemMatriculaAtiva();
    }

    public List<Map<String, Object>> listarCatequizandosComMatriculaAtiva() throws SQLException {
        return dao.listarCatequizandosComMatriculaAtiva();
    }

    public List<Map<String, Object>> listarTurmasComVaga() throws SQLException {
        return dao.listarTurmasComVaga();
    }

    // ---------- Getters e setters ----------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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