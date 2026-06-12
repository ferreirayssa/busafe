package com.transcol.busafe.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.transcol.busafe.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Service
public class TokenService {
    
    @Value("${JWT_SECRET:#{null}}")
    private String jwtSecret;
    
    private SecretKey key;
    private final long expirationTime = 86400000; // 1 dia (24 horas)
    
    /**
     * Inicializa a chave de assinatura
     * - Se JWT_SECRET existe no .env: usa ela
     * - Se não existe: gera automaticamente (desenvolvimento)
     */
    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            // DESENVOLVIMENTO: Gera chave automática
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            return;
        }

        // PRODUÇÃO: Usa a chave do ambiente.
        // Tenta interpretar como Base64; se não for Base64 válido, usa os bytes UTF-8.
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (Exception e) {
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }

        // HS256 exige chave de no mínimo 256 bits (32 bytes).
        // Se o segredo for curto demais, deriva uma chave de 256 bits via SHA-256.
        if (keyBytes.length < 32) {
            try {
                keyBytes = MessageDigest.getInstance("SHA-256").digest(keyBytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-256 indisponível para derivar a chave JWT", e);
            }
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Gera token JWT com claims personalizadas
     */
    public String gerarToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("nome", user.getNome());
        claims.put("email", user.getEmail());
        claims.put("tipoUsuario", user.getTipoUsuario().name());
        claims.put("plano", user.getPlano().name());
        
        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }

    /**
     * Extrai o Subject (email) do token
     */
    public String getSubject(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extrai todas as claims do token
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extrai uma claim específica do token
     */
    public String getClaim(String token, String claimName) {
        Claims claims = getClaims(token);
        return claims.get(claimName, String.class);
    }

    /**
     * Extrai o tipoUsuario do token
     */
    public String getTipoUsuario(String token) {
        return getClaim(token, "tipoUsuario");
    }

    /**
     * Extrai o plano do token
     */
    public String getPlano(String token) {
        return getClaim(token, "plano");
    }

    /**
     * Verifica se o token é válido
     */
    public boolean validarToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrai o ID do usuário do token
     */
    public String getUserId(String token) {
        return getClaim(token, "id");
    }
    
    /**
     * Método utilitário para gerar chave segura (use apenas 1 vez para gerar o .env)
     */
    public static String gerarChaveSecreta() {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}