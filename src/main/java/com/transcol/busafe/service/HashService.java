package com.transcol.busafe.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class HashService {
    
    @Value("${busafe.hash.salt:BusafeS3cur1tyS@lt2024!}")
    private String salt;
    
    /**
     * Gera hash SHA-256 com salt
     * @param input - dado a ser hasheado
     * @return hash em Base64
     */
    public String gerarHash(String input) {
        if (input == null || input.isEmpty()) return null;
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            String saltedInput = salt + input;
            byte[] hash = digest.digest(saltedInput.getBytes(StandardCharsets.UTF_8));
            
            return Base64.getEncoder().encodeToString(hash);
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash", e);
        }
    }
    
    public boolean verificarHash(String input, String hash) {
        if (input == null || hash == null) return false;
        String novoHash = gerarHash(input);
        return novoHash != null && novoHash.equals(hash);
    }
}