package com.example.catesys.Model;

import java.util.Date;

public class Parametrizacao {
    private int id;
    private String nome;
    private String cnpj;
    private String responsavel;
    private String telefone;
    private String email;
    private String site;
    private Date dataCriacao;
    private String endereco;
    private String bairro;
    private String cidade;
    private String cep;
    private String uf;
    private String logotipoGrande;
    private String logotipoPequeno;

    public Parametrizacao(String nome, String cnpj, String responsavel) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.responsavel = responsavel;
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

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
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

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public Date getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Date dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getLogotipoGrande() {
        return logotipoGrande;
    }

    public void setLogotipoGrande(String logotipoGrande) {
        this.logotipoGrande = logotipoGrande;
    }

    public String getLogotipoPequeno() {
        return logotipoPequeno;
    }

    public void setLogotipoPequeno(String logotipoPequeno) {
        this.logotipoPequeno = logotipoPequeno;
    }
}
