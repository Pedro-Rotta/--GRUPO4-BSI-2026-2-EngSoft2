package com.example.catesys.Model;

import com.example.catesys.Control.Conexao;
import com.example.catesys.DAO.LocalDAO;

import java.sql.SQLException;
import java.util.List;

// Local físico da catequese (tabela "sala").
public class Local {
    private int id;
    private String nome;
    private int capacidade;

    public Local(String nome, int capacidade) {
        this.nome = nome;
        this.capacidade = capacidade;
    }

    public Local() {
    }

    public static Local reconstituir(int id, String nome, int capacidade) {
        Local local = new Local(nome, capacidade);
        local.id = id;
        return local;
    }

    // Cálculo próprio: o local comporta essa quantidade de vagas?
    public boolean comporta(int vagas) {
        return vagas <= capacidade;
    }

    // Persistência: a Model instancia o DAO (Control → Model → DAO).

    public Local buscarPorId(int id, Conexao conexao) throws SQLException {
        return new LocalDAO(conexao).buscarPorId(id);
    }

    public List<Local> buscarTodos(Conexao conexao) throws SQLException {
        return new LocalDAO(conexao).buscarTodos();
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }
}
