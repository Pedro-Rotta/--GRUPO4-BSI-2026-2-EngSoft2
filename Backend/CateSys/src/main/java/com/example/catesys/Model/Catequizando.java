package com.example.catesys.Model;

import com.example.catesys.Control.Conexao;
import com.example.catesys.DAO.CatequizandoDAO;
import com.example.catesys.DAO.IDAO;
import com.example.catesys.Model.Enums.Sexo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Catequizando {
    private int id;
    private String nome;
    private Date dataNascimento;
    private String nomeResponsavel;
    private String telefoneResponsavel;
    private String documento;
    private String email;
    private String endereco;
    private String bairro;
    private String cidade;
    private String uf;
    private String cep;
    private String emailResponsavel;
    private String telefone;
    private Sexo sexo;

    public Catequizando(String nome, Date dataNascimento, String nomeResponsavel, String telefoneResponsavel, String documento) {
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.nomeResponsavel = nomeResponsavel;
        this.telefoneResponsavel = telefoneResponsavel;
        this.documento = documento;
    }

    public Catequizando() {
    }

    public Catequizando(int id) {
        this.id = id;
    }

    public static Catequizando reconstituir(
            int id,
            String nome,
            Date dataNascimento,
            String nomeResponsavel,
            String telefoneResponsavel) {

        Catequizando c = new Catequizando(
                nome,
                dataNascimento,
                nomeResponsavel,
                telefoneResponsavel,
                null
        );

        c.id = id;

        return c;
    }

    private static final String REGEX_TELEFONE = "\\(\\d{2}\\) \\d{4,5}-\\d{4}";
    private static final String REGEX_EMAIL = "^[\\w.+-]+@[\\w-]+\\.[\\w.]+$";
    private static final String REGEX_CEP = "\\d{5}-\\d{3}";
    private static final String REGEX_UF = "[A-Z]{2}";
    public static final int IDADE_SEM_RESPONSAVEL = 18;

    // Idade em anos completos na data de hoje (-1 se não houver data de nascimento).
    public int calcularIdade() {
        if (dataNascimento == null)
            return -1;
        LocalDate nascimento = new java.sql.Date(dataNascimento.getTime()).toLocalDate();
        return Period.between(nascimento, LocalDate.now()).getYears();
    }

    public boolean maiorDeIdade() {
        return calcularIdade() >= IDADE_SEM_RESPONSAVEL;
    }

    private static final Date DATA_MINIMA = java.sql.Date.valueOf("1926-01-01");

    private static boolean contemNumero(String valor) {
        return valor.chars().anyMatch(Character::isDigit);
    }

    // Pelo menos duas palavras separadas por espaço (nome + sobrenome).
    private static boolean temSobrenome(String valor) {
        return valor.trim().split("\\s+").length >= 2;
    }

    public List<String> validar() {
        List<String> erros = new ArrayList<>();

        if (vazio(nome))
            erros.add("Nome é obrigatório");
        else if (contemNumero(nome))
            erros.add("Nome não pode conter números");
        else if (!temSobrenome(nome))
            erros.add("Informe nome e sobrenome");
        if (dataNascimento == null)
            erros.add("Data de nascimento é obrigatória");
        else if (dataNascimento.after(new Date()))
            erros.add("Data de nascimento não pode ser futura");
        else if (dataNascimento.before(DATA_MINIMA))
            erros.add("Data de nascimento deve ser a partir de 01/01/1926");

        // Responsável só é obrigatório para menor de idade.
        boolean exigeResponsavel = !maiorDeIdade();
        if (vazio(nomeResponsavel)) {
            if (exigeResponsavel)
                erros.add("Nome do responsável é obrigatório para menores de " + IDADE_SEM_RESPONSAVEL + " anos");
        } else if (contemNumero(nomeResponsavel))
            erros.add("Nome do responsável não pode conter números");
        else if (!temSobrenome(nomeResponsavel))
            erros.add("Informe nome e sobrenome do responsável");
        if (vazio(telefoneResponsavel)) {
            if (exigeResponsavel)
                erros.add("Telefone do responsável é obrigatório para menores de " + IDADE_SEM_RESPONSAVEL + " anos");
        } else if (!telefoneResponsavel.matches(REGEX_TELEFONE))
            erros.add("Telefone do responsável inválido (formato: (99) 99999-9999)");

        if (!vazio(telefone) && !telefone.matches(REGEX_TELEFONE))
            erros.add("Telefone inválido (formato: (99) 99999-9999)");
        if (!vazio(email) && !email.matches(REGEX_EMAIL))
            erros.add("E-mail inválido");
        if (!vazio(emailResponsavel) && !emailResponsavel.matches(REGEX_EMAIL))
            erros.add("E-mail do responsável inválido");
        if (!vazio(cep) && !cep.matches(REGEX_CEP))
            erros.add("CEP inválido (formato: 99999-999)");
        if (!vazio(uf) && !uf.matches(REGEX_UF))
            erros.add("UF inválida (use a sigla com 2 letras, ex.: SP)");

        tamanhoMaximo(erros, "Nome", nome, 100);
        tamanhoMaximo(erros, "Nome do responsável", nomeResponsavel, 100);
        tamanhoMaximo(erros, "Documento", documento, 20);
        tamanhoMaximo(erros, "E-mail", email, 45);
        tamanhoMaximo(erros, "Endereço", endereco, 45);
        tamanhoMaximo(erros, "Bairro", bairro, 45);
        tamanhoMaximo(erros, "Cidade", cidade, 45);
        tamanhoMaximo(erros, "E-mail do responsável", emailResponsavel, 45);

        return erros;
    }

    // Persistência: a Model instancia o DAO e se passa como parâmetro (Control → Model → DAO).
    // A Conexao vem da Control, que é quem faz commit/rollback.

    public boolean gravar(Conexao conexao) throws SQLException {
        IDAO<Catequizando> dao = new CatequizandoDAO(conexao);
        return dao.gravar(this);
    }

    public boolean alterar(Conexao conexao) throws SQLException {
        IDAO<Catequizando> dao = new CatequizandoDAO(conexao);
        return dao.alterar(this);
    }

    public boolean excluir(Conexao conexao) throws SQLException {
        IDAO<Catequizando> dao = new CatequizandoDAO(conexao);
        return dao.excluir(id);
    }

    public Catequizando buscarPorId(int id, Conexao conexao) throws SQLException {
        IDAO<Catequizando> dao = new CatequizandoDAO(conexao);
        return dao.buscarPorId(id);
    }

    public List<Catequizando> buscar(String filtroNome, String filtroResponsavel, Conexao conexao) throws SQLException {
        CatequizandoDAO dao = new CatequizandoDAO(conexao); // buscar(nome, responsavel) é só deste DAO
        if (vazio(filtroNome) && vazio(filtroResponsavel))
            return dao.buscarTodos();
        return dao.buscar(filtroNome, filtroResponsavel);
    }

    public boolean possuiMatricula(Conexao conexao) throws SQLException {
        CatequizandoDAO dao = new CatequizandoDAO(conexao);
        return dao.possuiMatricula(id);
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static void tamanhoMaximo(List<String> erros, String campo, String valor, int maximo) {
        if (valor != null && valor.length() > maximo)
            erros.add(campo + " deve ter no máximo " + maximo + " caracteres");
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

    public Date getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(Date dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getNomeResponsavel() {
        return nomeResponsavel;
    }

    public void setNomeResponsavel(String nomeResponsavel) {
        this.nomeResponsavel = nomeResponsavel;
    }

    public String getTelefoneResponsavel() {
        return telefoneResponsavel;
    }

    public void setTelefoneResponsavel(String telefoneResponsavel) {
        this.telefoneResponsavel = telefoneResponsavel;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf == null ? null : uf.trim().toUpperCase();
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getEmailResponsavel() {
        return emailResponsavel;
    }

    public void setEmailResponsavel(String emailResponsavel) {
        this.emailResponsavel = emailResponsavel;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }
}
