package com.example.catesys.security;

import com.example.catesys.DAO.SecretariaDAO;
import com.example.catesys.Model.Secretaria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.sql.SQLException;
import java.util.ArrayList;

@Configuration
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private SecretariaDAO secretariaDAO;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Secretaria secretaria = this.secretariaDAO.buscarPorEmail(username);
            return new org.springframework.security.core.userdetails.User(secretaria.getEmail(), secretaria.getSenha(), new ArrayList<>());
        } catch (SQLException e) {
            throw new UsernameNotFoundException("Usuario nao encontrado");
        }
    }
}
