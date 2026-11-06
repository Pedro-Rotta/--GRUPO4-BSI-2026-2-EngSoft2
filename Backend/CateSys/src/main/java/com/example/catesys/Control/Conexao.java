package com.example.catesys.Control;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conexao {
    private static Conexao instancia;
    private Connection connection;

    private Conexao() throws SQLException {
        Properties props = new Properties();
        try (InputStream in = Conexao.class.getResourceAsStream("/application.properties")) {
            props.load(in);
        } catch (Exception e) {
            throw new SQLException("Não foi possível ler application.properties", e);
        }
        connection = DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password"));
        connection.setAutoCommit(false); // a Control decide commit/rollback
    }

    public static synchronized Conexao getInstancia() throws SQLException {
        if (instancia == null || instancia.connection.isClosed()
                || !instancia.connection.isValid(2))
            instancia = new Conexao(); // reabre se o Neon derrubou a conexão ociosa
        return instancia;
    }

    public Connection getConnection() { return connection; }

    public void commit() throws SQLException { connection.commit(); }
    public void rollback() throws SQLException { connection.rollback(); }
}