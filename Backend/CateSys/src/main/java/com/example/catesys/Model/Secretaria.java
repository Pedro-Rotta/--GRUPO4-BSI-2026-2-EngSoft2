package com.example.catesys.Model;

import com.example.catesys.Control.Conexao;
import com.example.catesys.DAO.IDAO;
import com.example.catesys.DAO.SecretariaDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Usuário de acesso total (secretaria paroquial).
public class Secretaria {
    private static final String REGEX_EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    public static final int TAMANHO_MINIMO_SENHA = 6;

    private int id;
    private String nome;
    private String cpf;
    private String email;
    private String senha;
    private String telefone;
    private Date dataAdmissao;
    private Date dataDemissao;

    // Primeiro acesso: a Control instancia a secretaria com os dados do request.
    public Secretaria(String nome, String cpf, String email, String senha) {
        setNome(nome);
        setCpf(cpf);
        setEmail(email);
        this.senha = senha;
    }

    // Consultas: a Control instancia uma Model vazia e chama a busca.
    public Secretaria() {
    }

    // Usado pelo DAO para montar a secretaria vinda do banco.
    public static Secretaria reconstituir(int id, String nome, String cpf, String email, String senha,
                                          String telefone, Date dataAdmissao, Date dataDemissao) {
        Secretaria s = new Secretaria(nome, cpf, email, senha);
        s.id = id;
        s.telefone = telefone;
        s.dataAdmissao = dataAdmissao;
        s.dataDemissao = dataDemissao;
        return s;
    }

    // Valida os dados do cadastro. A senha é validada antes de virar hash.
    public List<String> validar() {
        List<String> erros = new ArrayList<>();
        if (vazio(nome))
            erros.add("Nome é obrigatório");
        if (vazio(cpf) || !cpf.matches("\\d{11}"))
            erros.add("CPF deve ter 11 dígitos");
        if (vazio(email) || !email.matches(REGEX_EMAIL))
            erros.add("E-mail inválido");
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA)
            erros.add("A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres");
        return erros;
    }

    // Usuário desativado (com data de demissão já passada) não pode entrar.
    public boolean estaAtiva() {
        return dataDemissao == null || dataDemissao.after(new Date());
    }

    // Persistência: a Model instancia o DAO com a Conexao recebida da Control.

    public boolean gravar(Conexao conexao) throws SQLException {
        IDAO<Secretaria> dao = new SecretariaDAO(conexao);
        return dao.gravar(this);
    }

    // Login por e-mail ou CPF.
    public Secretaria buscarPorLogin(String login, Conexao conexao) throws SQLException {
        return new SecretariaDAO(conexao).buscarPorLogin(login);
    }

    // Política de primeiro acesso: só existe enquanto não há nenhum usuário cadastrado.
    public boolean existeAlguma(Conexao conexao) throws SQLException {
        return new SecretariaDAO(conexao).existeAlguma();
    }

    // Login duplicado: outro usuário já usa este e-mail ou CPF?
    public boolean loginDuplicado(Conexao conexao) throws SQLException {
        SecretariaDAO dao = new SecretariaDAO(conexao);
        return dao.buscarPorLogin(email) != null || dao.buscarPorLogin(cpf) != null;
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = normalizar(nome);
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf == null ? null : normalizar(cpf.replaceAll("\\D", ""));
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : normalizar(email.toLowerCase());
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
