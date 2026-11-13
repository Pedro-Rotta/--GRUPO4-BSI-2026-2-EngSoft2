package com.example.catesys.Model;

import com.example.catesys.DAO.ParametrizacaoDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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

    // A Model instanciando a DAO
    private ParametrizacaoDAO dao;

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Set<String> UFS = Set.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
            "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    // Construtor que a DAO usa para montar o objeto com o que leu do banco
    public Parametrizacao(String nome, String cnpj, String responsavel) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.responsavel = responsavel;
    }

    // Recebe a conexão da Control e cria a DAO com ela
    public Parametrizacao(Connection conn) {
        this.dao = new ParametrizacaoDAO(conn);
    }

    // ---------- Operações ----------

    // Retorna a parametrização gravada, ou null se ainda não foi feita
    public Parametrizacao buscar() throws SQLException {
        return dao.buscar();
    }

    public boolean existe() throws SQLException {
        return dao.buscar() != null;
    }

    // Se a tabela está vazia insere, senão altera.
    // Retorna true se inseriu e false se alterou.
    public boolean salvar() throws SQLException {
        if (dao.buscar() == null) {
            dao.gravar(this);
            return true;
        }
        dao.alterar(this);
        return false;
    }

    // Preenche com os dados da tela
    public void preencher(Map<String, String> d) {
        this.nome = d.get("nome");
        this.cnpj = d.get("cnpj");
        this.responsavel = d.get("responsavel");
        this.telefone = d.get("telefone");
        this.email = d.get("email");
        this.site = d.get("site");
        this.endereco = d.get("endereco");
        this.bairro = d.get("bairro");
        this.cidade = d.get("cidade");
        this.cep = d.get("cep");
        this.uf = d.get("uf");
        this.logotipoGrande = d.get("logotipoGrande");
        this.logotipoPequeno = d.get("logotipoPequeno");
    }

    // ---------- Validação ----------

    // Valida os campos se voltar vazio pode salvar.
    public Map<String, String> validar() {
        normalizar();
        Map<String, String> erros = new LinkedHashMap<>();

        if (vazio(nome)) erros.put("nome", "Informe o nome.");
        else limite(erros, "nome", nome, 100);

        if (vazio(cnpj)) erros.put("cnpj", "Informe o CNPJ.");
        else if (!cnpjValido(cnpj)) erros.put("cnpj", "CNPJ inválido.");

        if (vazio(responsavel)) erros.put("responsavel", "Informe o responsável.");
        else limite(erros, "responsavel", responsavel, 100);

        if (!vazio(telefone) && telefone.length() != 10 && telefone.length() != 11)
            erros.put("telefone", "Telefone inválido. Informe o DDD e o número.");

        if (!vazio(email) && (email.length() > 100 || !EMAIL.matcher(email).matches()))
            erros.put("email", "E-mail inválido.");

        if (!vazio(cep) && cep.length() != 8)
            erros.put("cep", "CEP inválido. Informe 8 números.");

        if (!vazio(uf) && !UFS.contains(uf))
            erros.put("uf", "UF inválida.");

        limite(erros, "site", site, 100);
        limite(erros, "endereco", endereco, 100);
        limite(erros, "bairro", bairro, 80);
        limite(erros, "cidade", cidade, 80);
        limite(erros, "logotipoGrande", logotipoGrande, 100);
        limite(erros, "logotipoPequeno", logotipoPequeno, 100);

        return erros;
    }

    // No banco ficam só com números (CNPJ, CEP, Telefone)
    private void normalizar() {
        nome = aparar(nome);
        responsavel = aparar(responsavel);
        email = aparar(email);
        site = aparar(site);
        endereco = aparar(endereco);
        bairro = aparar(bairro);
        cidade = aparar(cidade);
        logotipoGrande = aparar(logotipoGrande);
        logotipoPequeno = aparar(logotipoPequeno);
        cnpj = soDigitos(cnpj);
        cep = soDigitos(cep);
        telefone = soDigitos(telefone);
        uf = (uf == null) ? null : uf.trim().toUpperCase();
    }

    private String aparar(String s) {
        return (s == null) ? null : s.trim();
    }

    private String soDigitos(String s) {
        return (s == null) ? null : s.replaceAll("\\D", "");
    }

    private boolean vazio(String s) {
        return s == null || s.isBlank();
    }

    private void limite(Map<String, String> erros, String campo, String valor, int max) {
        if (valor != null && valor.length() > max)
            erros.put(campo, "Máximo de " + max + " caracteres.");
    }

    //Valida cnpj
    private boolean cnpjValido(String c) {
        // 14 dígitos q nao sejam iguais
        if (c.length() != 14 || c.matches("(\\d)\\1{13}")) return false;
        return digitoCnpj(c, 12) == c.charAt(12) - '0'
                && digitoCnpj(c, 13) == c.charAt(13) - '0';
    }

    // Calcula dígito verificador
    private int digitoCnpj(String c, int qtd) {
        int peso = 2;
        int soma = 0;
        for (int i = qtd - 1; i >= 0; i--) {
            soma += (c.charAt(i) - '0') * peso;
            peso = (peso == 9) ? 2 : peso + 1;
        }
        int resto = soma % 11;
        return (resto < 2) ? 0 : 11 - resto;
    }

    // ---------- Getters e setters ----------

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