package com.ecommerce.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.auth.entity.RefreshToken;
import com.ecommerce.auth.entity.User;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    void deleteByUser(User user);
}