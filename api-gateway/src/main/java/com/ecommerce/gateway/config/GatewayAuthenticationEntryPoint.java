package com.ecommerce.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class GatewayAuthenticationEntryPoint
        implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> commence(
            ServerWebExchange exchange,
            AuthenticationException exception) {

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "status", 401,
                "message", "Authentication is required or token is invalid"
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);

            return exchange.getResponse()
                    .writeWith(
                            Mono.just(
                                    exchange.getResponse()
                                            .bufferFactory()
                                            .wrap(bytes)
                            )
                    );

        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}