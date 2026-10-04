package com.ecommerce.gateway.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Component
public class GatewayAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

	private static final Logger log = LoggerFactory.getLogger(GatewayAuthenticationEntryPoint.class);

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException exception) {

		log.warn("Authentication failed for request: {} {} - Reason: {}", exchange.getRequest().getMethod(),
				exchange.getRequest().getURI().getPath(), exception.getMessage());

		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

		exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

		// Build a consistent JSON response for authentication failures.
		Map<String, Object> body = Map.of("status", HttpStatus.UNAUTHORIZED.value(), "message",
				"Authentication is required or token is invalid");

		try {
			byte[] bytes = objectMapper.writeValueAsBytes(body);

			return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));

		} catch (Exception e) {

			log.error("Failed to create authentication error response for {} {}", exchange.getRequest().getMethod(),
					exchange.getRequest().getURI().getPath(), e);

			return Mono.error(e);
		}
	}
}