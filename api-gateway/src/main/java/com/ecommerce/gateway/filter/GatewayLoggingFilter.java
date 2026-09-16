package com.ecommerce.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class GatewayLoggingFilter implements GlobalFilter, Ordered {
	
    private static final Logger log =LoggerFactory.getLogger(GatewayLoggingFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange,GatewayFilterChain chain) {

        long startTime = System.currentTimeMillis();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();
        log.info("Gateway Request: method={}, path={}", method, path);
        
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    long timeTaken =System.currentTimeMillis() - startTime;
                    int statusCode = exchange.getResponse()
                            .getStatusCode() != null
                            ? exchange.getResponse()
                                    .getStatusCode()
                                    .value()
                            : 0;
                    
                    log.info("Gateway Response: method={}, path={}, status={}, timeTaken={}ms",method,path,statusCode,timeTaken);
                }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}