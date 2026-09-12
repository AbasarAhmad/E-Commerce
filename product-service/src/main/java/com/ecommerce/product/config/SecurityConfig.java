package com.ecommerce.product.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ecommerce.product.security.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger log =LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        log.info("Configuring Spring Security filter chain");
        http
            .csrf(csrf -> {
                log.info("CSRF protection disabled");
                csrf.disable();
            })

            .sessionManagement(session -> {
                log.info("Configuring stateless session management");
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);})

            .authorizeHttpRequests
            (auth -> auth
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/api/v1/auth/login").permitAll()

                .requestMatchers(HttpMethod.DELETE,
                    "/api/v1/products/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter,UsernamePasswordAuthenticationFilter.class);

        log.info("JWT authentication filter registered successfully");
        log.debug("Spring Security filter chain configured successfully");
        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        log.info("Creating AuthenticationManager");

        AuthenticationManager authenticationManager =
                configuration.getAuthenticationManager();

        log.info("AuthenticationManager created successfully");

        return authenticationManager;
    }

    @Bean
    PasswordEncoder passwordEncoder() {

        log.info("Creating BCryptPasswordEncoder");

        PasswordEncoder passwordEncoder =
                new BCryptPasswordEncoder();

        log.info("BCryptPasswordEncoder created successfully");

        return passwordEncoder;
    }
}
