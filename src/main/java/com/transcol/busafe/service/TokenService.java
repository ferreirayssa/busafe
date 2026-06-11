package com.transcol.busafe.service;

import com.transcol.busafe.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenService {
    
    // Chave secreta para assinar o token
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long expirationTime = 86400000; // 1 dia (24 horas)

    /**
     * Gera token JWT com claims personalizadas (tipoUsuario, plano)
     */
    public String gerarToken(User user) {
        // Claims personalizadas
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("nome", user.getNome());
        claims.put("email", user.getEmail());
        claims.put("tipoUsuario", user.getTipoUsuario().name()); // PESSOA_FISICA ou PESSOA_JURIDICA
        claims.put("plano", user.getPlano().name()); // FREE, INDIVIDUAL, EMPRESARIAL
        
        return Jwts.builder()
                .setClaims(claims) // Adiciona as claims personalizadas
                .setSubject(user.getEmail()) // Subject = email
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
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
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
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
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
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
}