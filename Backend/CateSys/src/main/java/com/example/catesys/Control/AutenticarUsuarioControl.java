package com.example.catesys.Control;

import com.example.catesys.Model.Secretaria;
import com.example.catesys.security.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

// Controle de acesso: login (gera o token JWT) e política de primeiro acesso.
@RestController
@RequestMapping("/auth")
public class AutenticarUsuarioControl {

    public record LoginRequest(String login, String senha) {}

    public record PrimeiroAcessoRequest(String nome, String cpf, String email, String senha) {}

    public record LoginResponse(String nome, String token) {}

    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AutenticarUsuarioControl(PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public synchronized ResponseEntity<?> login(@RequestBody LoginRequest request) throws SQLException {
        if (vazio(request.login()) || vazio(request.senha()))
            return ResponseEntity.badRequest().body(Map.of("erros", List.of("Informe o login e a senha")));

        Conexao conexao = Conexao.getInstancia();
        try {
            Secretaria secretaria = new Secretaria().buscarPorLogin(request.login(), conexao);
            if (secretaria == null || !senhaConfere(request.senha(), secretaria.getSenha()))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erros", List.of("Login ou senha incorretos")));
            if (!secretaria.estaAtiva())
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erros", List.of("Usuário desativado")));
            return ResponseEntity.ok(new LoginResponse(secretaria.getNome(), tokenService.generateToken(secretaria)));
        } finally {
            conexao.rollback(); // só leitura: encerra a transação
        }
    }

    // A tela de login pergunta se o sistema ainda não tem usuário (primeiro acesso).
    @GetMapping("/primeiro-acesso")
    public synchronized Map<String, Boolean> primeiroAcesso() throws SQLException {
        Conexao conexao = Conexao.getInstancia();
        try {
            return Map.of("primeiroAcesso", !new Secretaria().existeAlguma(conexao));
        } finally {
            conexao.rollback();
        }
    }

    // Cria o usuário de acesso total inicial. Só funciona uma vez: enquanto não existe nenhum usuário.
    @PostMapping("/primeiro-acesso")
    public synchronized ResponseEntity<?> cadastrarPrimeiroAcesso(@RequestBody PrimeiroAcessoRequest request)
            throws SQLException {
        Secretaria secretaria = new Secretaria(request.nome(), request.cpf(), request.email(), request.senha());
        List<String> erros = secretaria.validar();
        if (!erros.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("erros", erros));

        Conexao conexao = Conexao.getInstancia();
        try {
            if (secretaria.existeAlguma(conexao)) {
                conexao.rollback();
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erros", List.of("O primeiro acesso já foi realizado")));
            }
            secretaria.setSenha(passwordEncoder.encode(request.senha())); // grava só o hash
            secretaria.gravar(conexao);
            conexao.commit();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new LoginResponse(secretaria.getNome(), tokenService.generateToken(secretaria)));
        } catch (SQLException | RuntimeException e) {
            conexao.rollback();
            throw e;
        }
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<?> erroBanco(SQLException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erros", List.of("Erro ao acessar o banco de dados")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> requisicaoInvalida(Exception e) {
        return ResponseEntity.badRequest()
                .body(Map.of("erros", List.of("Requisição inválida: verifique os campos enviados")));
    }

    // Senhas antigas gravadas sem hash não são aceitas: só confere hash BCrypt.
    private boolean senhaConfere(String senha, String hash) {
        return hash != null && hash.startsWith("$2") && passwordEncoder.matches(senha, hash);
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
