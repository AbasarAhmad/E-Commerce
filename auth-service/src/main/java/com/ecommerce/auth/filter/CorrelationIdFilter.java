package com.ecommerce.auth.filter;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
	private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
	
	private static final String CORRELATION_ID = "correlationId";
	private static final String HEADER_NAME = "X-Correlation-Id";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String correlationId = request.getHeader(HEADER_NAME);

		if (correlationId == null || correlationId.isBlank()) {
			correlationId = UUID.randomUUID().toString();
		}

		try {
			MDC.put(CORRELATION_ID, correlationId);
			log.info("CorrelationIdFilter set correlationId: {}", correlationId);
			response.setHeader(HEADER_NAME, correlationId);

			filterChain.doFilter(request, response);
		} finally {
			MDC.remove(CORRELATION_ID);
		}
	}
}