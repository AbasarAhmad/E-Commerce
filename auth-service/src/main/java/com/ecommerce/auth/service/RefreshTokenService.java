package com.ecommerce.auth.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.auth.entity.RefreshToken;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

    private static final Logger log =LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {

        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        RefreshToken savedToken =refreshTokenRepository.save(refreshToken);
        log.info("Refresh token created for user: {}",user.getUsername());
        return savedToken;
    }

    public RefreshToken findByToken(String token) {

        return refreshTokenRepository.findByToken(token)
                .map(this::verifyExpiration).orElseThrow(() ->new RuntimeException("Refresh token not found"));
    }

    public RefreshToken verifyExpiration(RefreshToken refreshToken) {

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {

            log.warn("Refresh token expired for user: {}",refreshToken.getUser().getUsername());
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token has expired");
        }
        return refreshToken;
    }

    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {

        User user = oldToken.getUser();
        refreshTokenRepository.delete(oldToken);
        RefreshToken newToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        RefreshToken savedToken =refreshTokenRepository.save(newToken);
        log.info("Refresh token rotated for user: {}",user.getUsername());
        return savedToken;
    }

    @Transactional
    public void revokeToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElseThrow(() ->
                                new RuntimeException("Refresh token not found"));

        refreshTokenRepository.delete(refreshToken);
        log.info("Refresh token revoked for user: {}",refreshToken.getUser().getUsername());
    }
}
