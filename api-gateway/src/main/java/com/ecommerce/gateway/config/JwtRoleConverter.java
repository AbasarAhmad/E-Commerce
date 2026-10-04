package com.ecommerce.gateway.config;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import reactor.core.publisher.Mono;

public class JwtRoleConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

	private static final Logger log = LoggerFactory.getLogger(JwtRoleConverter.class);

	@Override
	public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {

		String username = jwt.getSubject();
		String role = jwt.getClaimAsString("role");

		if (role == null || role.isBlank()) {

			log.warn("No role found in JWT for user: {}", username);

			return Mono.just(new JwtAuthenticationToken(jwt, Collections.emptyList()));
		}

		// Spring Security expects roles with the ROLE_ prefix.
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

		log.info("JWT role converted for user: {} with authority: {}", username, authority.getAuthority());

		return Mono.just(new JwtAuthenticationToken(jwt, Collections.singletonList(authority)));
	}
}