package com.ecommerce.product.security;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final CustomUserDetailsService userDetailsService;

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(JwtService jwtService,CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain filterChain)
    		throws ServletException, IOException {

        log.info("Processing {} request for URI: {} HttpServletRequest : {} ",request.getMethod(), request.getRequestURI(), request);

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null) {
            log.info("Authorization header is missing for URI: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        
        if (!authHeader.startsWith("Bearer ")) {
            log.info("Authorization header does not contain a Bearer token for URI: {}",  request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        String username;
        String role;
        try {
            username = jwtService.extractUsername(jwt);
             role = jwtService.extractRole(jwt);
            log.info("Username extracted successfully from JWT: {}", username);

        } catch (Exception exception) {
            log.warn("Failed to extract username from JWT: {}", 
                    exception.getMessage());

            filterChain.doFilter(request, response);
            return;
        }

        if (username != null&& SecurityContextHolder.getContext().getAuthentication() == null) {
        	
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            log.info("User details loaded successfully for username: {}", username);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                log.info("JWT token is valid for username: {}", username);

                List<GrantedAuthority> authorities =List.of(new SimpleGrantedAuthority("ROLE_" + role));

                UsernamePasswordAuthenticationToken authentication =new UsernamePasswordAuthenticationToken(
                                userDetails,null,authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("User authenticated successfully: {}", username);

            } else{
                log.warn("JWT token validation failed for username: {}", username);
            }
        }

        filterChain.doFilter(request, response);
    }
}

