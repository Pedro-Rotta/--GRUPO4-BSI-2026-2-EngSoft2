package com.example.catesys.DAO;

import java.sql.SQLException;
import java.util.List;

public interface IDAO<T> {
    boolean gravar(T obj) throws SQLException;
    boolean alterar(T obj) throws SQLException;
    boolean excluir(int id) throws SQLException;
    T buscarPorId(int id) throws SQLException;
    List<T> buscarTodos() throws SQLException;
}