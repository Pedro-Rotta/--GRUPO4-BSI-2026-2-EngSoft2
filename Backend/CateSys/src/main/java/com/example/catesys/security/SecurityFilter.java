package com.example.catesys.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Lê o token do cabeçalho Authorization e autentica a requisição.
// Não consulta o banco: o token só é emitido no login, depois de conferir senha e usuário ativo,
// e assim o filtro não disputa a Conexao (Singleton) com as transações das Controls.
@Component
public class SecurityFilter extends OncePerRequestFilter {
    private final TokenService tokenService;

    public SecurityFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = recuperarToken(request);
        if (token != null) {
            String login = tokenService.validateToken(token);
            if (login != null) {
                var autenticacao = new UsernamePasswordAuthenticationToken(login, null,
                        List.of(new SimpleGrantedAuthority("ROLE_SECRETARIA")));
                SecurityContextHolder.getContext().setAuthentication(autenticacao);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        String cabecalho = request.getHeader("Authorization");
        if (cabecalho == null || !cabecalho.startsWith("Bearer "))
            return null;
        return cabecalho.substring("Bearer ".length());
    }
}
