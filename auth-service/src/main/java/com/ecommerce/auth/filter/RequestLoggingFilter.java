package com.ecommerce.auth.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
	private static final long SLOW_REQUEST_THRESHOLD_MS = 1000;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		long startTime = System.currentTimeMillis();

		try {
			filterChain.doFilter(request, response);
		} finally {
			long executionTime = System.currentTimeMillis() - startTime;

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			String username = authentication != null ? authentication.getName() : "anonymous";

			if (executionTime >= SLOW_REQUEST_THRESHOLD_MS) {

				log.warn("Slow HTTP request: method={} uri={} status={} executionTime={}ms", request.getMethod(),
						request.getRequestURI(), response.getStatus(), executionTime);

			} else {

				log.info("HTTP request completed: method={} uri={} status={} executionTime={}ms", request.getMethod(),
						request.getRequestURI(), response.getStatus(), executionTime);
			}
		}
	}
}