package com.hrms.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // AES encryption secret (must be 16, 24, or 32 chars)
    @Value("${jwt.encryption.secret}")
    private String encryptionSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    private SecretKeySpec getEncryptionKey() {
        return new SecretKeySpec(encryptionSecret.getBytes(), "AES");
    }

    // ---------------- Token Generation ----------------

    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        // 1. Generate signed JWT
        String jwt = Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        // 2. Encrypt the JWT before returning
        return encrypt(jwt);
    }

    // ---------------- Token Parsing ----------------

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String encryptedToken) {
        // 1. Decrypt AES-encrypted token back to raw JWT
        String decryptedJwt = decrypt(encryptedToken);

        // 2. Parse the raw JWT normally with signing key
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(decryptedJwt)
                .getBody();
    }

    // ---------------- Validation ----------------

    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ---------------- AES Encryption Helpers ----------------

    private String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, getEncryptionKey());
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting token", e);
        }
    }

    private String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, getEncryptionKey());
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            return new String(cipher.doFinal(decoded));
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting token", e);
        }
    }
}
