package com.ecommerce.gateway.config;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    // JWT secret is read from application.yml.
    // The same secret must be used by Auth Service to generate the JWT.
    @Value("${jwt.secret}")
    private String jwtSecret;


    @Bean
    public ReactiveJwtDecoder jwtDecoder() {

        /*
         * Convert the JWT secret string into a cryptographic key.
         *
         * HmacSHA384 is used because Auth Service signs the JWT
         * using an HMAC SHA-384 algorithm (HS384).
         *
         * The Gateway must use the same algorithm and secret
         * to successfully validate the JWT.
         */
        SecretKeySpec secretKey = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8),"HmacSHA384");

        /*
         * Configure the Gateway to validate incoming JWT tokens.
         *
         * withSecretKey() -> uses our shared secret for validation.
         *
         * HS384 -> tells Spring that the JWT was signed using
         * HMAC SHA-384.
         *
         * If Auth Service and Gateway use different algorithms,
         * JWT validation will fail.
         */
        return NimbusReactiveJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS384)
                .build();
    }


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http) {

        return http

                // JWT is used for authentication, so CSRF protection
                // is disabled for this stateless REST API.
                .csrf(ServerHttpSecurity.CsrfSpec::disable)


                .authorizeExchange(exchange -> exchange

                        /*
                         * These authentication APIs are public.
                         *
                         * User does not have a JWT before login,
                         * therefore these endpoints cannot require authentication.
                         */
                        .pathMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh"
                        ).permitAll()


                        /*
                         * DELETE product operation is restricted to ADMIN.
                         *
                         * hasRole("ADMIN") internally checks for:
                         * ROLE_ADMIN
                         *
                         * The ROLE_ADMIN authority is created below
                         * from the "role" claim inside the JWT.
                         */
                        .pathMatchers(
                                org.springframework.http.HttpMethod.DELETE,
                                "/api/v1/products/**").hasRole("ADMIN")


                        /*
                         * Every other API must have a valid JWT.
                         *
                         * Example:
                         * GET /api/v1/products
                         * PUT /api/v1/products/1
                         *
                         * These requests require authentication.
                         */
                        .anyExchange().authenticated()

                )


                /*
                 * Enable OAuth2 Resource Server JWT authentication.
                 *
                 * Gateway extracts the Bearer token from:
                 *
                 * Authorization: Bearer <JWT>
                 *
                 * Then the JWT decoder validates the token.
                 */
                .oauth2ResourceServer(oauth2 -> oauth2

                        .jwt(jwt -> jwt

                                // Convert JWT claims into Spring Security
                                // authorities such as ROLE_ADMIN or ROLE_USER.
                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                )

                .build();
    }


    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {

        /*
         * JwtAuthenticationConverter converts information
         * from the JWT into Spring Security authorities.
         */
        JwtAuthenticationConverter converter =new JwtAuthenticationConverter();

        /*
         * Instead of using Spring's default role mapping,
         * we use our own method to extract the "role" claim.
         *
         * Example JWT:
         *
         * {
         *     "sub": "user",
         *     "role": "ADMIN"
         * }
         */
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities
        );

        /*
         * API Gateway uses reactive WebFlux security,
         * so the normal JwtAuthenticationConverter is wrapped
         * in a reactive adapter.
         */
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }


    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {

        /*
         * Read the "role" claim from the JWT.
         *
         * Example:
         *
         * "role": "ADMIN"
         *
         * role = "ADMIN"
         */
        String role = jwt.getClaimAsString("role");


        /*
         * If JWT does not contain a role,
         * don't give the user any authority.
         *
         * This prevents missing/invalid roles from
         * accidentally getting access to role-protected APIs.
         */
        if (role == null || role.isBlank()) {
            return List.of();
        }


        /*
         * Spring Security expects roles in the format:
         *
         * ROLE_ADMIN
         * ROLE_USER
         *
         * Therefore:
         *
         * JWT role = ADMIN
         *          ↓
         * ROLE_ADMIN
         *
         * Then hasRole("ADMIN") can match it.
         */
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

}
