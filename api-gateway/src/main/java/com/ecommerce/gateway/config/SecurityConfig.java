package com.ecommerce.gateway.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /*
     * JWT secret is read from application.yml.
     *
     * The same secret must be used by Auth Service
     * to generate/sign the JWT.
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /*
     * JWT Decoder
     *
     * Auth Service is currently using HS384,
     * so Gateway must also use HS384 to validate
     * the JWT.
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {

        SecretKeySpec secretKey = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA384"
        );

        return NimbusReactiveJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS384)
                .build();
    }

    /*
     * Spring Security configuration for API Gateway.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http) {

        return http

                /*
                 * JWT-based REST API is stateless,
                 * so CSRF protection is disabled.
                 */
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchange -> exchange

                        // Public authentication APIs
                        .pathMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh"
                        ).permitAll()

                        // Product read operations
                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/v1/products",
                                "/api/v1/products/**"
                        ).hasAnyRole("USER", "ADMIN")

                        // Product write operations
                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/v1/products",
                                "/api/v1/products/**"
                        ).hasRole("ADMIN")

                        .pathMatchers(
                                HttpMethod.PUT,
                                "/api/v1/products",
                                "/api/v1/products/**"
                        ).hasRole("ADMIN")

                        .pathMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/products",
                                "/api/v1/products/**"
                        ).hasRole("ADMIN")

                        // Everything else requires authentication
                        .anyExchange().authenticated()
                )

                /*
                 * Enable JWT authentication.
                 *
                 * Gateway extracts:
                 *
                 * Authorization: Bearer <JWT>
                 *
                 * Then validates the JWT and extracts
                 * the user's role.
                 */
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        new ReactiveJwtAuthenticationConverterAdapter(
                                                jwtAuthenticationConverter()
                                        )
                                )
                        )
                )

                .build();
    }

    /*
     * Converts the "role" claim from JWT
     * into Spring Security authorities.
     *
     * Example:
     *
     * JWT:
     * {
     *     "sub": "riyaz",
     *     "role": "USER"
     * }
     *
     * becomes:
     *
     * ROLE_USER
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            /*
             * Read role from JWT.
             */
            String role = jwt.getClaimAsString("role");

            /*
             * If role is missing or empty,
             * don't grant any authority.
             */
            if (role == null || role.isBlank()) {
                return List.of();
            }

            /*
             * Convert:
             *
             * USER  -> ROLE_USER
             * ADMIN -> ROLE_ADMIN
             *
             * This allows:
             *
             * hasAnyRole("USER", "ADMIN")
             */
            return List.of(
                    new SimpleGrantedAuthority("ROLE_" + role)
            );
        });

        return converter;
    }
}

