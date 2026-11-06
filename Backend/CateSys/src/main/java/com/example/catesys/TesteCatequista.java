
package com.example.catesys;

import com.example.catesys.Control.GerenciarCatequistaControl;
import com.example.catesys.Model.Catequista;

import java.sql.Date;

public class TesteCatequista {

    public static void main(String[] args) {

        GerenciarCatequistaControl control =
                new GerenciarCatequistaControl();

        String cpfTeste = "11144477735";
        int idTeste = 0;

        try {
            // Verificar se o CPF ja existe
            if (control.buscarPorCpf(cpfTeste) != null) {
                System.out.println("CPF ja cadastrado. Teste cancelado.");
                return;
            }

            // 1 - CADASTRAR
            Catequista c = new Catequista(
                    "Catequista Teste",
                    cpfTeste,
                    "18999999999",
                    "teste@exemplo.com",
                    null
            );

            c.setDataInicio(Date.valueOf("2026-11-06"));

            boolean cadastrou = control.cadastrar(c);
            System.out.println("CADASTRAR: " + cadastrou);

            if (!cadastrou) {
                return;
            }

            // 2 - CONSULTAR
            Catequista encontrado = control.buscarPorCpf(cpfTeste);

            if (encontrado == null) {
                throw new IllegalStateException("Cadastro nao encontrado");
            }

            idTeste = encontrado.getId();

            System.out.println("CONSULTAR: " + encontrado.getNome());
            System.out.println("ID: " + idTeste);

            // 3 - TESTAR CPF DUPLICADO
            try {
                Catequista duplicado = new Catequista(
                        "Outro Catequista",
                        cpfTeste,
                        "18888888888",
                        "outro@exemplo.com",
                        null
                );

                control.cadastrar(duplicado);
                System.out.println("ERRO: CPF duplicado foi aceito");

            } catch (IllegalArgumentException e) {
                System.out.println("CPF DUPLICADO: Bloqueado");
            }

            // 4 - ALTERAR
            encontrado.setTelefone("18888888888");

            boolean alterou = control.alterar(encontrado);
            System.out.println("ALTERAR: " + alterou);

            // 5 - CONFIRMAR ALTERACAO
            Catequista atualizado = control.buscarPorId(idTeste);

            if (atualizado != null) {
                System.out.println(
                        "NOVO TELEFONE: " + atualizado.getTelefone()
                );
            }

            // 6 - LISTAR TODOS
            System.out.println(
                    "TOTAL DE CATEQUISTAS: " +
                            control.buscarTodos().size()
            );

            // 7 - BUSCAR POR NOME
            System.out.println(
                    "BUSCAR POR NOME: " +
                            control.buscarPorNome("Catequista Teste").size()
            );

            // 8 - EXCLUIR
            boolean excluiu = control.excluir(idTeste);
            System.out.println("EXCLUIR: " + excluiu);

            // 9 - CONFIRMAR EXCLUSAO
            Catequista excluido = control.buscarPorId(idTeste);

            System.out.println(
                    "SELECT APOS DELETE: " +
                            (excluido == null ? "Nao encontrado" : "Encontrado")
            );

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            // Limpa somente o registro criado no teste
            if (idTeste > 0) {
                try {
                    Catequista restante = control.buscarPorId(idTeste);

                    if (restante != null
                            && cpfTeste.equals(restante.getCpf())) {

                        control.excluir(idTeste);
                        System.out.println("LIMPEZA: Registro removido");
                    }

                } catch (Exception e) {
                    System.err.println(
                            "ATENCAO: verificar registro de teste ID " + idTeste
                    );
                    e.printStackTrace();
                }
            }
        }
    }
}
