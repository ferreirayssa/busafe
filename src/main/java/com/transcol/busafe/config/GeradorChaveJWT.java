package com.transcol.busafe.config;

import java.util.Base64;

import javax.crypto.SecretKey;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Execute esta classe uma única vez para gerar a chave JWT de produção
 */
public class GeradorChaveJWT {
    public static void main(String[] args) {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
        
        System.out.println("============================================");
        System.out.println("🔑 Sua chave JWT para produção:");
        System.out.println("============================================");
        System.out.println(base64Key);
        System.out.println("============================================");
        System.out.println("Copie esta chave e adicione no .env:");
        System.out.println("JWT_SECRET=" + base64Key);
        System.out.println("============================================");
        System.out.println("⚠️  GUARDE ESTA CHAVE EM LOCAL SEGURO!");
        System.out.println("⚠️  NÃO COMMITE NO GITHUB!");
        System.out.println("============================================");
    }
}