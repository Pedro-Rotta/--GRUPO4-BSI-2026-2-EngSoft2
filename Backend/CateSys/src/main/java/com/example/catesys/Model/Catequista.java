
package com.example.catesys.Model;

import com.example.catesys.DAO.CatequistaDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class Catequista {

    private int id;
    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    private Secretaria secretaria;
    private Date dataInicio;
    private Date dataFim;
    private String senha;

    public Catequista() {
    }

    public Catequista(String nome, String cpf, String telefone,
                      String email, Secretaria secretaria) {
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
        this.secretaria = secretaria;
    }

    // OPERACOES COM O DAO

    public boolean gravar(Connection conexao) throws SQLException {
        CatequistaDAO dao = new CatequistaDAO(conexao);
        return dao.gravar(this);
    }

    public boolean alterar(Connection conexao) throws SQLException {
        CatequistaDAO dao = new CatequistaDAO(conexao);
        return dao.alterar(this);
    }

    public boolean excluir(Connection conexao, int id)
            throws SQLException {
        CatequistaDAO dao = new CatequistaDAO(conexao);
        return dao.excluir(id);
    }

    public Catequista buscarPorId(Connection conexao, int id)
            throws SQLException {
        CatequistaDAO dao = new CatequistaDAO(conexao);
        return dao.buscarPorId(id);
    }

    public List<Catequista> buscarTodos(Connection conexao)
            throws SQLException {
        CatequistaDAO dao = new CatequistaDAO(conexao);
        return dao.buscarTodos();
    }

    public List<Catequista> buscarPorNome(
            Connection conexao, String nome) throws SQLException {
        CatequistaDAO dao = new CatequistaDAO(conexao);
        return dao.buscarPorNome(nome);
    }

    public Catequista buscarPorCpf(
            Connection conexao, String cpf) throws SQLException {
        CatequistaDAO dao = new CatequistaDAO(conexao);
        return dao.buscarPorCpf(cpf);
    }

    public List<Catequista> buscarPorCpfParcial(
            Connection conexao, String cpf) throws SQLException {
        CatequistaDAO dao = new CatequistaDAO(conexao);
        return dao.buscarPorCpfParcial(cpf);
    }

    // GETTERS E SETTERS

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Secretaria getSecretaria() {
        return secretaria;
    }

    public void setSecretaria(Secretaria secretaria) {
        this.secretaria = secretaria;
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

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    // VALIDACAO DOS DADOS

    public void validarDados() {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome obrigatorio");
        }

        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF obrigatorio");
        }

        if (!validarCpf(cpf)) {
            throw new IllegalArgumentException("CPF invalido");
        }

        if (email != null && !email.isBlank()) {
            if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                throw new IllegalArgumentException("Email invalido");
            }
        }

        if (dataInicio != null && dataFim != null) {
            if (dataFim.before(dataInicio)) {
                throw new IllegalArgumentException(
                        "Data fim nao pode ser anterior a data inicio"
                );
            }
        }
    }

    private boolean validarCpf(String cpf) {

        if (!cpf.matches("\\d{11}")) {
            return false;
        }

        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        for (int tamanho = 9; tamanho <= 10; tamanho++) {

            int soma = 0;

            for (int i = 0; i < tamanho; i++) {
                int digito = cpf.charAt(i) - '0';
                soma += digito * (tamanho + 1 - i);
            }

            int resto = (soma * 10) % 11;

            if (resto == 10) {
                resto = 0;
            }

            if (resto != cpf.charAt(tamanho) - '0') {
                return false;
            }
        }

        return true;
    }
}