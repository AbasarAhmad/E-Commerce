package com.ecommerce.auth.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final Logger log =LoggerFactory.getLogger(JwtService.class);

    private final SecretKey secretKey;

    private final long expirationTime;

    public JwtService(@Value("${jwt.secret}") String secret,@Value("${jwt.expiration}") long expirationTime) {

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expirationTime;
    }

    public String generateToken(String username, String role) {

        Date now = new Date();
        Date expiration =new Date(now.getTime() + expirationTime);
        String token = Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();

        log.info("JWT generated for user: {}", username);
        return token;
    }

    public String extractUsername(String token) {

        String username= Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        log.info("Extracted User from JWT is : {}", username);
        return username;
    }

    public String extractRole(String token) {

        String role= Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
        
        log.info("Extracted Role from JWT is : {}", role);
        return role;
    }

    public boolean isTokenValid(String token,UserDetails userDetails) {

        String username = extractUsername(token);

        Boolean result= username.equals(userDetails.getUsername())&& !isTokenExpired(token);
        log.info("TokenValidation : {}", result);
       return result;
    }

    
    private boolean isTokenExpired(String token) {

        Date expiration =Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                        .getExpiration();

        return expiration.before(new Date());
    }
}

