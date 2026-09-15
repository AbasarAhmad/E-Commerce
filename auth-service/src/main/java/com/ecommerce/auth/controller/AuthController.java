package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.AuthRequest;
import com.ecommerce.auth.dto.AuthResponse;
import com.ecommerce.auth.dto.RefreshTokenRequest;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.entity.RefreshToken;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.security.JwtService;
import com.ecommerce.auth.service.RefreshTokenService;
import com.ecommerce.auth.service.UserService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Authentication APIs",description = "APIs for user registration and authentication")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log =
            LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public AuthController(
            UserService userService,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            UserRepository userRepository) {

        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Register a new user",description = "Creates a new user account with a BCrypt-hashed password")
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerUser(request.getUsername(),request.getPassword());
        log.info("User registered successfully with id: {}", user.getId());
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User registered successfully with id: " + user.getId());
    }
    
    
    @Operation(summary = "Authenticate user",description = "Authenticates username and password and returns JWT access and refresh tokens")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()));

        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority()
                .replace("ROLE_", "");

        String accessToken = jwtService.generateToken(authentication.getName(),role);

        User user = userRepository.findByUsername(
                request.getUsername())
                .orElseThrow(() ->new UsernameNotFoundException("User not found: "+ request.getUsername()));

        RefreshToken refreshToken =refreshTokenService.createRefreshToken(user);
        log.info("User logged in successfully: {}", user.getUsername());

        return ResponseEntity.ok(new AuthResponse(accessToken,refreshToken.getToken()));
    }
    
    
    @Operation(summary = "Refresh access token",description = "Generates a new access token and rotates the refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {

        RefreshToken refreshToken =refreshTokenService.findByToken( request.getRefreshToken());
        User user = refreshToken.getUser();
        String accessToken = jwtService.generateToken(user.getUsername(),user.getRole());
        RefreshToken newRefreshToken =refreshTokenService.rotateRefreshToken(refreshToken);
        log.info("Access token refreshed for user: {}",user.getUsername());

        return ResponseEntity.ok(new AuthResponse(accessToken,newRefreshToken.getToken()));
    }

    
    @Operation(summary = "Logout user",description = "Revokes the supplied refresh token")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {

        refreshTokenService.revokeToken(request.getRefreshToken());
        log.info("User logged out successfully");
    }
}
