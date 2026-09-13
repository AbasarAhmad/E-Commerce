package com.ecommerce.product.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.product.dto.AuthRequest;
import com.ecommerce.product.dto.AuthResponse;
import com.ecommerce.product.dto.RefreshTokenRequest;
import com.ecommerce.product.entity.RefreshToken;
import com.ecommerce.product.entity.User;
import com.ecommerce.product.repository.UserRepository;
import com.ecommerce.product.security.JwtService;
import com.ecommerce.product.security.RefreshTokenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log =LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;
    
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager,JwtService jwtService,
            UserRepository userRepository,RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
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
        
        String accessToken =jwtService.generateToken(request.getUsername(),role);
        
        log.info("Login successful and JWT token generated for username: {}",request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() ->new UsernameNotFoundException( "User not found: "+ request.getUsername()));

        RefreshToken refreshToken =refreshTokenService.createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }
    
    
    @PostMapping("/refresh")
    public AuthResponse refreshToken(@Valid @RequestBody RefreshTokenRequest request) {

        RefreshToken refreshToken =refreshTokenService.findByToken(request.getRefreshToken());

        User user = refreshToken.getUser();
        String role = user.getRole();

        String accessToken =jwtService.generateToken(user.getUsername(),role);
        log.info("Access token refreshed for user: {} token: {}", user.getUsername(), refreshToken);
        
        RefreshToken newRefreshToken =refreshTokenService.rotateRefreshToken(refreshToken);

        return new AuthResponse(accessToken,newRefreshToken.getToken());
    }
    
    
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @Valid @RequestBody RefreshTokenRequest request) {

        refreshTokenService.revokeToken(
                request.getRefreshToken());
    }
}
