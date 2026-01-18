
package com.example.springreact.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    

    // Clave secreta (en producción usar application.properties y cifrada)
    // Usa una clave base64 segura y persistente
    private static final String SECRET = "bXktdmVyeS1zZWNyZXQta2V5LXdpdGgtc3VmZmljaWVudC1sZW5ndGgtZm9yLWhzMjU2IQ=="; // ejemplo, reemplazar en producción
    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET));
    
    // Access token: 15 minutos
    private final long ACCESS_TOKEN_VALIDITY = 15 * 60 * 1000; // 15 min
    
    // Refresh token: 7 días
    private final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 días
    
    public String generateAccessToken(String email, Long userId, String name, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("name", name);
        claims.put("role", role);
        claims.put("type", "access");
        
        return createToken(claims, email, ACCESS_TOKEN_VALIDITY);
    }
    
    public String generateRefreshToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        
        return createToken(claims, email, REFRESH_TOKEN_VALIDITY);
    }
    
    private String createToken(Map<String, Object> claims, String subject, long validity) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validity);

        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(SECRET_KEY)
            .compact();
    }
    
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }
    
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }
    
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }
    
    public String extractType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }
    
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(SECRET_KEY)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
    
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    public boolean validateToken(String token, String email) {
        try {
            String tokenEmail = extractEmail(token);
            return (tokenEmail.equals(email) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isAccessToken(String token) {
        try {
            return "access".equals(extractType(token));
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(extractType(token));
        } catch (Exception e) {
            return false;
        }
    }
}
