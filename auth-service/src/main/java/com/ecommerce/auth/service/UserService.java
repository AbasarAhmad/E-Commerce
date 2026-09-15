package com.ecommerce.auth.service;

import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.exception.DuplicateUsernameException;
import com.ecommerce.auth.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String username, String password) {

        if (userRepository.existsByUsername(username)) {
            log.info("Registration failed. Username already exists: {}", username);
            
            throw new DuplicateUsernameException("Username already exists: " + username);
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role("USER")
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", username);
        return savedUser;
    }
}

