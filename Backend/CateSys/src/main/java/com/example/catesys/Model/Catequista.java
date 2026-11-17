package com.example.catesys.Model;

import com.example.catesys.Control.Conexao;
import com.example.catesys.DAO.CatequistaDAO;
import com.example.catesys.DAO.IDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Catequista {
    private static final String REGEX_EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private int id;
    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    private Integer secretariaId;
    private Date dataInicio;
    private Date dataFim;
    private String senha;

    // Cadastro: a Control instancia o catequista com os dados do request.
    public Catequista(String nome, String cpf, String telefone, String email,
                      Integer secretariaId, Date dataInicio, Date dataFim) {
        setNome(nome);
        setCpf(cpf);
        setTelefone(telefone);
        setEmail(email);
        this.secretariaId = secretariaId;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    // Alteração: mesmos dados do cadastro, mais o id do registro.
    public Catequista(int id, String nome, String cpf, String telefone, String email,
                      Integer secretariaId, Date dataInicio, Date dataFim) {
        this(nome, cpf, telefone, email, secretariaId, dataInicio, dataFim);
        this.id = id;
    }

    // Exclusão: só o id.
    public Catequista(int id) {
        this.id = id;
    }

    // Consultas: a Control instancia uma Model vazia e chama a busca.
    public Catequista() {
    }

    // Usado pelo DAO para montar o catequista vindo do banco.
    public static Catequista reconstituir(int id, String nome, String cpf, String telefone, String email,
                                          Integer secretariaId, Date dataInicio, Date dataFim) {
        return new Catequista(id, nome, cpf, telefone, email, secretariaId, dataInicio, dataFim);
    }

    // GRASP Especialista: o catequista valida os próprios dados e devolve todos os erros.
    public List<String> validar() {
        List<String> erros = new ArrayList<>();
        if (vazio(nome))
            erros.add("Nome é obrigatório");
        else if (nome.length() > 100)
            erros.add("Nome deve ter no máximo 100 caracteres");
        if (vazio(cpf))
            erros.add("CPF é obrigatório");
        else if (!cpfValido(cpf))
            erros.add("CPF inválido");
        if (!vazio(telefone) && !telefone.matches("\\d{10,11}"))
            erros.add("Telefone inválido: informe DDD e número");
        if (!vazio(email) && !email.matches(REGEX_EMAIL))
            erros.add("E-mail inválido");
        if (secretariaId != null && secretariaId <= 0)
            erros.add("Secretaria inválida");
        if (dataInicio != null && dataFim != null && dataFim.before(dataInicio))
            erros.add("Data de término não pode ser anterior à data de início");
        return erros;
    }

    // Catequista com data de término passada não pode receber turma.
    public boolean estaAtivo() {
        return dataFim == null || dataFim.after(new Date());
    }

    // Persistência: a Model instancia o DAO (Control → Model → DAO).
    // A Conexao vem da Control, que é quem faz commit/rollback.

    public boolean gravar(Conexao conexao) throws SQLException {
        IDAO<Catequista> dao = new CatequistaDAO(conexao);
        return dao.gravar(this);
    }

    public boolean alterar(Conexao conexao) throws SQLException {
        IDAO<Catequista> dao = new CatequistaDAO(conexao);
        return dao.alterar(this);
    }

    public boolean excluir(Conexao conexao) throws SQLException {
        IDAO<Catequista> dao = new CatequistaDAO(conexao);
        return dao.excluir(id);
    }

    public Catequista buscarPorId(int id, Conexao conexao) throws SQLException {
        IDAO<Catequista> dao = new CatequistaDAO(conexao);
        return dao.buscarPorId(id);
    }

    public List<Catequista> buscarTodos(Conexao conexao) throws SQLException {
        IDAO<Catequista> dao = new CatequistaDAO(conexao);
        return dao.buscarTodos();
    }

    // Filtros da tela de consulta (nenhum por código).
    public List<Catequista> buscar(String filtroNome, String filtroCpf, Conexao conexao) throws SQLException {
        return new CatequistaDAO(conexao).buscar(filtroNome, filtroCpf);
    }

    // Outro catequista já usa este CPF?
    public boolean cpfDuplicado(Conexao conexao) throws SQLException {
        return new CatequistaDAO(conexao).existeCpf(cpf, id);
    }

    // Catequista vinculado a turma não pode ser excluído.
    public boolean possuiTurma(Conexao conexao) throws SQLException {
        return new CatequistaDAO(conexao).possuiTurma(id);
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static boolean cpfValido(String cpf) {
        if (!cpf.matches("\\d{11}") || cpf.matches("(\\d)\\1{10}"))
            return false;
        for (int tamanho = 9; tamanho <= 10; tamanho++) {
            int soma = 0;
            for (int i = 0; i < tamanho; i++)
                soma += (cpf.charAt(i) - '0') * (tamanho + 1 - i);
            int resto = (soma * 10) % 11;
            if (resto == 10)
                resto = 0;
            if (resto != cpf.charAt(tamanho) - '0')
                return false;
        }
        return true;
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
        this.cpf = normalizar(cpf);
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = normalizar(telefone);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = normalizar(email);
    }

    public Integer getSecretariaId() {
        return secretariaId;
    }

    public void setSecretariaId(Integer secretariaId) {
        this.secretariaId = secretariaId;
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
}
