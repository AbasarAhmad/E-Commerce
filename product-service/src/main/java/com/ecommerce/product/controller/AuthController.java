package com.ecommerce.product.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.product.dto.AuthRequest;
import com.ecommerce.product.dto.AuthResponse;
import com.ecommerce.product.security.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log =
            LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {

        log.info("Login attempt for username: {}", request.getUsername());

        Authentication authentication =authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()));

        log.info("User authenticated successfully: {}",request.getUsername());

        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        log.info("Role retrieved for username {}: {}",request.getUsername(),role);

        role = role.replace("ROLE_", "");

        log.info("Role prepared for JWT claim for username {}: {}",request.getUsername(),role);

        String token = jwtService.generateToken(request.getUsername(),role);

        log.info("Login successful and JWT token generated for username: {}",request.getUsername());

        return new AuthResponse(token);
    }
}
