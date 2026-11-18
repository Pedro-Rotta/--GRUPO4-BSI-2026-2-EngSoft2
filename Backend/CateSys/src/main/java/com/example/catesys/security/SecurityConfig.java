package com.example.catesys.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// As telas (HTML/CSS/JS) são públicas; a API exige o token JWT gerado no login.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SecurityFilter securityFilter;

    public SecurityConfig(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/index.html", "/favicon.ico", "/error",
                                "/html/**", "/css/**", "/js/**", "/imgs/**", "/img/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers("/auth/primeiro-acesso").permitAll()
                        // Criar usuário pelo login: a própria Control exige login e senha de um administrador.
                        .requestMatchers(HttpMethod.POST, "/auth/usuarios").permitAll()
                        // Nome e logotipos da paróquia aparecem antes do login.
                        .requestMatchers(HttpMethod.GET, "/api/parametrizacao").permitAll()
                        .anyRequest().authenticated()
                )
                // Sem token (ou token vencido): 401, e o front volta para a tela de login.
                .exceptionHandling(erros -> erros.authenticationEntryPoint(
                        (request, response, e) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
