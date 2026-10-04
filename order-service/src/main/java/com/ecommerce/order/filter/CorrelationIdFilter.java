package com.ecommerce.order.filter;

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

			log.info("No correlation ID found in request. Generated new correlation ID: {}", correlationId);
		} else {
			log.info("Using correlation ID from request: {}", correlationId);
		}

		try {
			/*
			 * Store the correlation ID in MDC so it is automatically included in
			 * application logs during the request processing.
			 */
			MDC.put(CORRELATION_ID, correlationId);

			// Return the correlation ID to the caller for end-to-end request tracing.
			response.setHeader(HEADER_NAME, correlationId);

			log.debug("Request processing started. method={}, uri={}, correlationId={}", request.getMethod(),
					request.getRequestURI(), correlationId);

			filterChain.doFilter(request, response);

			log.debug("Request processing completed. status={}, correlationId={}", response.getStatus(), correlationId);

		} finally {
			/*
			 * Servlet container threads are reused between requests. Removing the MDC value
			 * prevents the previous request's correlation ID from appearing in another
			 * request's logs.
			 */
			MDC.remove(CORRELATION_ID);
		}
	}
}
