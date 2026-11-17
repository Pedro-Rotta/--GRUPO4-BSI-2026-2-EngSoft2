
package com.example.catesys;

import com.example.catesys.Control.GerenciarCatequistaControl;
import com.example.catesys.Control.GerenciarCatequistaControl.CatequistaRequest;
import com.example.catesys.Control.GerenciarCatequistaControl.CatequistaResponse;

import java.time.LocalDate;
import java.util.List;

// Teste manual do RF_B3 contra o banco: cadastra, consulta, altera e exclui um catequista de teste.
public class TesteCatequista {

    public static void main(String[] args) {
        GerenciarCatequistaControl control = new GerenciarCatequistaControl();
        String cpfTeste = "11144477735";
        int idTeste = 0;

        try {
            if (!control.buscar(null, cpfTeste).isEmpty()) {
                System.out.println("CPF ja cadastrado. Teste cancelado.");
                return;
            }

            // 1 - CADASTRAR
            CatequistaRequest dados = new CatequistaRequest("Catequista Teste", cpfTeste, "18999999999",
                    "teste@exemplo.com", null, LocalDate.of(2026, 11, 6), null);
            System.out.println("CADASTRAR: " + control.cadastrar(dados).getStatusCode());

            // 2 - CONSULTAR POR CPF
            List<CatequistaResponse> encontrados = control.buscar(null, cpfTeste);
            if (encontrados.isEmpty())
                throw new IllegalStateException("Cadastro nao encontrado");
            idTeste = encontrados.get(0).id();
            System.out.println("CONSULTAR: " + encontrados.get(0).nome() + " (ID " + idTeste + ")");

            // 3 - CPF DUPLICADO (espera 409)
            CatequistaRequest duplicado = new CatequistaRequest("Outro Catequista", cpfTeste, "18888888888",
                    "outro@exemplo.com", null, null, null);
            System.out.println("CPF DUPLICADO: " + control.cadastrar(duplicado).getStatusCode());

            // 4 - VALIDACAO (espera 400 com todos os erros)
            CatequistaRequest invalido = new CatequistaRequest("", "123", "1", "x", null, null, null);
            System.out.println("INVALIDO: " + control.cadastrar(invalido).getBody());

            // 5 - ALTERAR
            CatequistaRequest alterado = new CatequistaRequest("Catequista Teste", cpfTeste, "18888888888",
                    "teste@exemplo.com", null, LocalDate.of(2026, 11, 6), null);
            System.out.println("ALTERAR: " + control.alterar(idTeste, alterado).getStatusCode());
            System.out.println("NOVO TELEFONE: " + control.buscar(null, cpfTeste).get(0).telefone());

            // 6 - BUSCAR POR NOME
            System.out.println("BUSCAR POR NOME: " + control.buscar("Catequista Teste", null).size());

            // 7 - EXCLUIR
            System.out.println("EXCLUIR: " + control.excluir(idTeste).getStatusCode());
            System.out.println("SELECT APOS DELETE: " + control.buscarPorId(idTeste).getStatusCode());
            idTeste = 0;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Limpa somente o registro criado no teste
            if (idTeste > 0) {
                try {
                    control.excluir(idTeste);
                    System.out.println("LIMPEZA: Registro removido");
                } catch (Exception e) {
                    System.err.println("ATENCAO: verificar registro de teste ID " + idTeste);
                    e.printStackTrace();
                }
            }
        }
    }
}
