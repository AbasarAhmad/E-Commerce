package com.ecommerce.product.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ecommerce.product.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    private static final Logger log =LoggerFactory.getLogger(CustomUserDetailsService.class);

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)throws UsernameNotFoundException {

        log.info("Loading user details for username: {}", username);

        com.ecommerce.product.entity.User user = userRepository
                .findByUsername(username).orElseThrow(() -> {log.warn("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);});

        log.info("User found successfully. Username: {}, Role: {}, Enabled: {}",user.getUsername(),user.getRole(), user.isEnabled());

        UserDetails userDetails = User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .disabled(!user.isEnabled())
                .build();

        log.info("UserDetails created successfully for username: {}",username);

        return userDetails;
    }
}

