package com.ecommerce.order.config;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfoConfig {

    @Bean
    public InfoContributor appInfo() {
        return endpoint -> endpoint
                .withDetail("name", "order-service")
                .withDetail("description", "E-Commerce Order Service")
                .withDetail("version", "1.0.0");
    }
}