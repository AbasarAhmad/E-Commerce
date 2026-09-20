package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.UserResponse;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.exception.DuplicateUsernameException;
import com.ecommerce.auth.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    public User registerUser(String username, String password, String role) {

        if (userRepository.existsByUsername(username)) {
            log.info("Registration failed. Username already exists: {}", username);
          
            throw new DuplicateUsernameException("Username already exists: " + username);
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", username);
        return savedUser;
    }
    
    public UserResponse getUserByUsername(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->new UsernameNotFoundException("User not found: " + username));

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
    }
    
  
}

