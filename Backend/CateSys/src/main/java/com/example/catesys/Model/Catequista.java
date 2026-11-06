
package com.example.catesys.Model;

import java.util.Date;

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

    public Catequista(String nome, String cpf, String telefone,
                      String email, Secretaria secretaria) {
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
        this.secretaria = secretaria;
    }

    public static Catequista reconstituir(
            int id,
            String nome,
            String cpf,
            String telefone,
            String email,
            Integer secretariaId,
            Date dataInicio,
            Date dataFim) {

        Secretaria secretaria = null;

        if (secretariaId != null) {
            secretaria = Secretaria.reconstituir(secretariaId);
        }

        Catequista c = new Catequista(
                nome, cpf, telefone, email, secretaria
        );

        c.id = id;
        c.dataInicio = dataInicio;
        c.dataFim = dataFim;

        return c;
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