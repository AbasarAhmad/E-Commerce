package com.ecommerce.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class GatewayAccessDeniedHandler
        implements ServerAccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(
            ServerWebExchange exchange,
            AccessDeniedException exception) {

        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "status", 403,
                "message", "Access denied"
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