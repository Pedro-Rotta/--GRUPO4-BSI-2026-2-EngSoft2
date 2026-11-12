package com.example.catesys.Control;

import com.example.catesys.DAO.SecretariaDAO;
import com.example.catesys.DTO.LoginRequestDTO;
import com.example.catesys.DTO.RegisterRequestDTO;
import com.example.catesys.DTO.ResponseDTO;
import com.example.catesys.Model.Secretaria;
import com.example.catesys.security.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final SecretariaDAO secretariaDAO;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthController(SecretariaDAO secretariaDAO, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.secretariaDAO = secretariaDAO;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequestDTO body) {
        try {
            Secretaria secretaria = this.secretariaDAO.buscarPorEmail(body.email());

            if (secretaria != null && passwordEncoder.matches(body.senha(), secretaria.getSenha())) {
                String token = this.tokenService.generateToken(secretaria);
                return ResponseEntity.ok(new ResponseDTO(secretaria.getNome(), token));
            }
            else {
                return ResponseEntity.status(401).build();
            }
        }
        catch(SQLException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequestDTO body) {
        try {
            Secretaria secretaria = this.secretariaDAO.buscarPorEmail(body.email());
            if(secretaria==null) {
                Secretaria nova = new Secretaria();
                nova.setNome(body.nome());
                nova.setCpf(body.cpf());
                nova.setEmail(body.email());
                nova.setSenha(passwordEncoder.encode(body.senha()));
                secretariaDAO.gravar(nova);

                String token = this.tokenService.generateToken(nova);
                return ResponseEntity.ok(new ResponseDTO(nova.getNome(), token));
            }
            return ResponseEntity.badRequest().build();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}