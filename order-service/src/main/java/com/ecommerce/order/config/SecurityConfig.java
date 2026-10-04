package com.ecommerce.order.config;

import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable())

				// JWT authentication is stateless, so the server does not maintain HTTP sessions.
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/**")
						.permitAll()
						.anyRequest()
						.authenticated())

				.oauth2ResourceServer(
						oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

		log.info("Security configuration initialized with stateless JWT authentication");

		return http.build();
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {

		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

		converter.setJwtGrantedAuthoritiesConverter(jwt -> {

			String role = jwt.getClaimAsString("role");

			if (role == null || role.isBlank()) {
				return List.of();
			}

			/*
			 * Spring Security expects roles in the ROLE_XXX format.
			 * The JWT contains the role without the ROLE_ prefix.
			 */
			return List.of(new SimpleGrantedAuthority("ROLE_" + role));
		});

		return converter;
	}

	@Bean
	public JwtDecoder jwtDecoder() {

		/*
		 * The same secret and HMAC algorithm used to create the JWT
		 * must be used here to validate its signature.
		 */
		SecretKeySpec secretKey = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA384");

		return NimbusJwtDecoder
				.withSecretKey(secretKey)
				.macAlgorithm(MacAlgorithm.HS384)
				.build();
	}
}