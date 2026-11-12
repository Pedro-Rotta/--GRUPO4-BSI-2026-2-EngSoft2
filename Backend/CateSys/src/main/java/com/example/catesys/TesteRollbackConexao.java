
package com.example.catesys;

import com.example.catesys.Control.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TesteRollbackConexao {

    public static void main(String[] args) {

        try {
            Conexao conexao = Conexao.getInstancia();

            synchronized (conexao) {

                Connection conn = conexao.getConnection();

                // Confirmar os dados iniciais
                if (!existeVinculo(conn, 1, 12)) {
                    throw new IllegalStateException(
                            "O vinculo original (Turma 1, Catequista 12) nao existe."
                    );
                }

                if (existeVinculo(conn, 1, 13)) {
                    throw new IllegalStateException(
                            "O catequista 13 ja pertence a Turma 1."
                    );
                }

                System.out.println("Estado inicial verificado.");

                boolean erroEsperado = false;

                try {

                    // Alterar temporariamente o vinculo
                    String update = """
                            UPDATE turma_catequista
                            SET catequista_catequista_id = 13
                            WHERE turma_turma_id = 1
                              AND catequista_catequista_id = 12
                            """;

                    try (PreparedStatement ps =
                                 conn.prepareStatement(update)) {

                        int alterados = ps.executeUpdate();

                        if (alterados != 1) {
                            throw new SQLException(
                                    "O UPDATE nao alterou exatamente um registro."
                            );
                        }
                    }

                    System.out.println("UPDATE executado.");

                    // Provocar chave duplicada
                    String insert = """
                            INSERT INTO turma_catequista (
                                catequista_catequista_id,
                                turma_turma_id
                            )
                            VALUES (13, 1)
                            """;

                    try (PreparedStatement ps =
                                 conn.prepareStatement(insert)) {

                        ps.executeUpdate();
                    }

                } catch (SQLException e) {

                    if (!"23505".equals(e.getSQLState())) {
                        throw e;
                    }

                    erroEsperado = true;

                    System.out.println(
                            "Erro esperado: chave duplicada."
                    );

                    System.out.println(
                            "SQLSTATE: " + e.getSQLState()
                    );

                } finally {

                    // Desfazer todas as alteracoes
                    conexao.rollback();

                    System.out.println(
                            "Rollback executado pelo Java."
                    );
                }

                if (!erroEsperado) {
                    throw new IllegalStateException(
                            "O erro de chave duplicada nao ocorreu."
                    );
                }

                // Conferir se o registro foi restaurado
                boolean originalExiste =
                        existeVinculo(conn, 1, 12);

                boolean temporarioExiste =
                        existeVinculo(conn, 1, 13);

                conexao.rollback();

                System.out.println(
                        "Vinculo original existe: " + originalExiste
                );

                System.out.println(
                        "Vinculo temporario existe: " + temporarioExiste
                );

                if (originalExiste && !temporarioExiste) {

                    System.out.println(
                            "TESTE APROVADO: rollback funcionou."
                    );

                } else {

                    System.out.println(
                            "TESTE REPROVADO: dados inconsistentes."
                    );
                }
            }

        } catch (Exception e) {

            System.err.println(
                    "Erro no teste: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    private static boolean existeVinculo(
            Connection conn,
            int turmaId,
            int catequistaId
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM turma_catequista
                WHERE turma_turma_id = ?
                  AND catequista_catequista_id = ?
                """;

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, turmaId);
            ps.setInt(2, catequistaId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
