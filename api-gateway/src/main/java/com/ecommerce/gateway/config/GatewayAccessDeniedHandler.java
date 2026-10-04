package com.ecommerce.gateway.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Component
public class GatewayAccessDeniedHandler implements ServerAccessDeniedHandler {

	private static final Logger log = LoggerFactory.getLogger(GatewayAccessDeniedHandler.class);

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException exception) {

		log.warn("Access denied for request: {} {} - Reason: {}", exchange.getRequest().getMethod(),
				exchange.getRequest().getURI().getPath(), exception.getMessage());

		exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
		exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

		// Build a consistent JSON response for forbidden requests.
		Map<String, Object> body = Map.of("status", HttpStatus.FORBIDDEN.value(), "message", "Access denied");

		try {
			byte[] bytes = objectMapper.writeValueAsBytes(body);

			return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));

		} catch (Exception e) {

			log.error("Failed to create access denied response for {} {}", exchange.getRequest().getMethod(),
					exchange.getRequest().getURI().getPath(), e);

			return Mono.error(e);
		}
	}
}