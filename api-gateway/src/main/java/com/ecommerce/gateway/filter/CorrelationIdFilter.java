package com.ecommerce.gateway.filter;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

	private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

	private static final String CORRELATION_ID = "correlationId";
	private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);

		if (correlationId == null || correlationId.isBlank()) {

			correlationId = UUID.randomUUID().toString();

			log.info("Correlation ID not provided. Generated new correlationId: {}", correlationId);

		} else {

			log.info("Received correlationId from request: {}", correlationId);
		}

		final String finalCorrelationId = correlationId;

		ServerHttpRequest request = exchange.getRequest().mutate().header(CORRELATION_ID_HEADER, finalCorrelationId)
				.build();

		ServerWebExchange mutatedExchange = exchange.mutate().request(request).build();

		mutatedExchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, finalCorrelationId);

		try {

			MDC.put(CORRELATION_ID, finalCorrelationId);

			log.info("Processing gateway request: {} {}", request.getMethod(), request.getURI().getPath());

			return chain.filter(mutatedExchange).doFinally(signal -> {
				MDC.remove(CORRELATION_ID);
			});

		} catch (Exception ex) {

			MDC.remove(CORRELATION_ID);

			log.error("Error processing gateway request with correlationId: {}", finalCorrelationId, ex);

			throw ex;
		}
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}