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
    private int localId;
    private int horarioId;

    // RF_F5 Ofertar Turma: a Control instancia a turma com os dados do request.
    // A turma nasce com todas as vagas livres e status OFERTADA; o ano vem da data de início.
    public Turma(Etapa etapa, Date dataInicio, Date dataFim, int vagasTotais,
                 int catequistaId, int localId, int horarioId) {
        this.etapa = etapa;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.anoOferta = dataInicio == null ? null : String.valueOf(paraLocalDate(dataInicio).getYear());
        this.vagasTotais = vagasTotais;
        this.vagasDisponiveis = vagasTotais;
        this.status = StatusTurma.OFERTADA;
        this.catequistaId = catequistaId;
        this.localId = localId;
        this.horarioId = horarioId;
    }

    public Turma() {
    }

    // Usado pelo DAO para montar a turma vinda do banco (com id, vagas e status gravados).
    public static Turma reconstituir(int id, Etapa etapa, String anoOferta, Date dataInicio, Date dataFim,
                                     int vagasTotais, int vagasDisponiveis, StatusTurma status,
                                     int catequistaId, int localId, int horarioId) {
        Turma t = new Turma();
        t.id = id;
        t.etapa = etapa;
        t.anoOferta = anoOferta;
        t.dataInicio = dataInicio;
        t.dataFim = dataFim;
        t.vagasTotais = vagasTotais;
        t.vagasDisponiveis = vagasDisponiveis;
        t.status = status;
        t.catequistaId = catequistaId;
        t.localId = localId;
        t.horarioId = horarioId;
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
        else if (vagasDisponiveis < 0)
            erros.add("Limite de vagas menor que as " + vagasOcupadas() + " matrículas da turma");
        else if (vagasDisponiveis > vagasTotais)
            erros.add("Vagas disponíveis devem ficar entre 0 e o limite de vagas");
        if (status == null)
            erros.add("Status é obrigatório");
        return erros;
    }

    // RF_F5: além das regras gerais, a oferta não pode começar antes de hoje.
    public List<String> validarOferta() {
        List<String> erros = validar();
        if (comecaAntesDeHoje())
            erros.add("A data de início não pode ser anterior à data atual");
        return erros;
    }

    public boolean comecaAntesDeHoje() {
        return dataInicio != null && paraLocalDate(dataInicio).isBefore(LocalDate.now());
    }

    // Turma encerrada ou cancelada não ocupa local nem agenda.
    public boolean estaAtiva() {
        return status != StatusTurma.ENCERRADA && status != StatusTurma.CANCELADA;
    }

    // Vagas já preenchidas por matrícula.
    public int vagasOcupadas() {
        return vagasTotais - vagasDisponiveis;
    }

    // RF_B5 Alterar Turma: troca os dados editáveis e recalcula o ano e as vagas livres,
    // mantendo as vagas já ocupadas por matrícula.
    public void alterarDados(Etapa etapa, Date dataInicio, Date dataFim, int vagasTotais, StatusTurma status,
                             int catequistaId, int localId, int horarioId) {
        int ocupadas = vagasOcupadas();
        this.etapa = etapa;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.anoOferta = dataInicio == null ? null : String.valueOf(paraLocalDate(dataInicio).getYear());
        this.vagasTotais = vagasTotais;
        this.vagasDisponiveis = vagasTotais - ocupadas;
        this.status = status;
        this.catequistaId = catequistaId;
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

    public boolean alterar(Conexao conexao) throws SQLException {
        return new TurmaDAO(conexao).alterar(this);
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
