package com.example.catesys.Model;

import java.util.Date;

public class Secretaria {
    private int id;
    private String nome;
    private String cpf;
    private String email;
    private String senha;
    private String telefone;
    private Date dataAdmissao;
    private Date dataDemissao;

    public Secretaria(String nome, String cpf, String email, String senha) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.senha = senha;
    }

    public Secretaria() {
    }

    public static Secretaria reconstituir(
            int id,
            String nome,
            String cpf,
            String email,
            String senha,
            String telefone,
            Date dataAdmissao,
            Date dataDemissao) {

        Secretaria s = new Secretaria(
                nome, cpf, email, senha
        );

        s.id = id;
        s.setTelefone(telefone);
        s.setDataAdmissao(dataAdmissao);
        s.setDataDemissao(dataDemissao);

        return s;
    }

    public static Secretaria reconstituir(Integer integer) {
        return new Secretaria();
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Date getDataAdmissao() {
        return dataAdmissao;
    }

    public void setDataAdmissao(Date dataAdmissao) {
        this.dataAdmissao = dataAdmissao;
    }

    public Date getDataDemissao() {
        return dataDemissao;
    }

    public void setDataDemissao(Date dataDemissao) {
        this.dataDemissao = dataDemissao;
    }
}
