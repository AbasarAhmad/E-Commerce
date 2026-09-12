package com.ecommerce.product.security;

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

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey secretKey;
    private final long expirationTime;

    public JwtService(@Value("${jwt.secret}") String secret,@Value("${jwt.expiration}") long expirationTime) {

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        this.expirationTime = expirationTime;
    }

    public String generateToken(String username, String role) {

        log.info("Generating JWT token for username: {}", username);
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);

        String token = Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
        log.info("JWT token generated successfully for username: {}", username);
        return token;
    }

    public String extractUsername(String token) {

        log.info("Extracting username from JWT token");

        String username = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        log.info("Username extracted successfully from JWT: {}", username);

        return username;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {

        String username = extractUsername(token);

        boolean usernameMatches = username.equals(userDetails.getUsername());
        boolean tokenExpired = isTokenExpired(token);

        boolean valid = usernameMatches && !tokenExpired;

        if (valid) {
            log.info("JWT token is valid for username: {}", username);
        } else {
            log.warn("JWT token validation failed for username: {}. Username matches: {}, Token expired: {}",
                    username,usernameMatches,tokenExpired);
        }

        return valid;
    }

    private boolean isTokenExpired(String token) {

        Date expiration = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();

        boolean expired = expiration.before(new Date());

        if (expired) {
            log.warn("JWT token has expired. Expiration time: {}", expiration);
        } else {
            log.info("JWT token is not expired. Expiration time: {}", expiration);
        }

        return expired;
    }
    
    
    public String extractRole(String token) {

        log.debug("Extracting role from JWT token");

        String role = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);

        log.debug("Role extracted successfully from JWT: {}", role);

        return role;
    }
}

