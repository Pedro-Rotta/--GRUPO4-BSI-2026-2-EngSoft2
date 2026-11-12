package com.example.catesys.Model;

import com.example.catesys.Control.Conexao;
import com.example.catesys.DAO.TurmaDAO;
import com.example.catesys.Model.Enums.Etapa;
import com.example.catesys.Model.Enums.StatusTurma;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public Turma() {
    }

    // RF_F5 Ofertar Turma: a turma nasce com todas as vagas livres e status OFERTADA.
    public static Turma ofertar(Etapa etapa, Date dataInicio, Date dataFim, int vagasTotais,
                                int catequistaId, int localId, int horarioId) {
        String ano = dataInicio == null ? null
                : String.valueOf(new java.sql.Date(dataInicio.getTime()).toLocalDate().getYear());
        return new Turma(etapa, ano, dataInicio, dataFim, vagasTotais, vagasTotais,
                StatusTurma.OFERTADA, catequistaId, 0, localId, horarioId);
    }

    public static Turma reconstituir(int id, Etapa etapa, String anoOferta, Date dataInicio, Date dataFim,
                                     int vagasTotais, int vagasDisponiveis, StatusTurma status,
                                     int catequistaId, int localId, int horarioId) {
        Turma t = new Turma(etapa, anoOferta, dataInicio, dataFim, vagasTotais, vagasDisponiveis,
                status, catequistaId, 0, localId, horarioId);
        t.id = id;
        return t;
    }

    public List<String> validar() {
        List<String> erros = new ArrayList<>();
        if (etapa == null)
            erros.add("Etapa é obrigatória");
        if (catequistaId <= 0)
            erros.add("Catequista é obrigatório");
        if (localId <= 0)
            erros.add("Local é obrigatório");
        if (horarioId <= 0)
            erros.add("Horário é obrigatório");
        if (dataInicio == null)
            erros.add("Data de início é obrigatória");
        if (dataFim == null)
            erros.add("Data de término é obrigatória");
        if (dataInicio != null && dataFim != null && !dataFim.after(dataInicio))
            erros.add("Data de término deve ser posterior à data de início");
        if (vagasTotais <= 0)
            erros.add("Limite de vagas deve ser maior que zero");
        if (vagasDisponiveis < 0 || vagasDisponiveis > vagasTotais)
            erros.add("Vagas disponíveis devem ficar entre 0 e o limite de vagas");
        return erros;
    }

    // Turma encerrada ou cancelada não ocupa local nem agenda e não pode mudar de local.
    public boolean estaAtiva() {
        return status != StatusTurma.ENCERRADA && status != StatusTurma.CANCELADA;
    }

    public void atribuirLocal(int localId, int horarioId) {
        this.localId = localId;
        this.horarioId = horarioId;
    }

    public static LocalDate paraLocalDate(Date data) {
        return data == null ? null : new java.sql.Date(data.getTime()).toLocalDate();
    }

    // Persistência: a Model instancia o DAO (Control → Model → DAO).
    // A Conexao vem da Control, que é quem faz commit/rollback.

    public boolean gravar(Conexao conexao) throws SQLException {
        return new TurmaDAO(conexao).gravar(this);
    }

    public boolean alterarLocal(Conexao conexao) throws SQLException {
        return new TurmaDAO(conexao).alterarLocal(this);
    }

    public Turma buscarPorId(int id, Conexao conexao) throws SQLException {
        return new TurmaDAO(conexao).buscarPorId(id);
    }

    public List<Turma> buscarTodas(Conexao conexao) throws SQLException {
        return new TurmaDAO(conexao).buscarTodas();
    }

    // Outra turma ativa usa o mesmo local, no mesmo dia/horário, em período sobreposto?
    public boolean existeConflitoLocal(Conexao conexao) throws SQLException {
        return new TurmaDAO(conexao).existeConflitoLocal(this);
    }

    // O catequista já tem outra turma ativa com horário sobreposto no período?
    public boolean existeConflitoCatequista(Conexao conexao) throws SQLException {
        return new TurmaDAO(conexao).existeConflitoCatequista(this);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
