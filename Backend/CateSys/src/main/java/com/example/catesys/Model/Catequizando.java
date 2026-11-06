package com.example.catesys.Model;

import com.example.catesys.Model.Enums.Sexo;

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

    public List<String> validar() {
        List<String> erros = new ArrayList<>();

        if (vazio(nome))
            erros.add("Nome é obrigatório");
        if (dataNascimento == null)
            erros.add("Data de nascimento é obrigatória");
        else if (dataNascimento.after(new Date()))
            erros.add("Data de nascimento não pode ser futura");
        if (vazio(nomeResponsavel))
            erros.add("Nome do responsável é obrigatório");
        if (telefoneResponsavel == null || !telefoneResponsavel.matches(REGEX_TELEFONE))
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
