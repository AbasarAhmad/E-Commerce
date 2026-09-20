package com.ecommerce.order.config;

import java.time.Duration;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    // Normal RestClient.Builder
    // Used by Eureka for http://localhost:8761
    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    // Load-balanced RestClient.Builder
    // Used for service-to-service communication
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    // RestClient for Auth Service
    @Bean
    public RestClient authServiceRestClient(@LoadBalanced RestClient.Builder loadBalancedRestClientBuilder) {

        JdkClientHttpRequestFactory requestFactory =new JdkClientHttpRequestFactory();

        requestFactory.setReadTimeout(Duration.ofSeconds(3));

        return loadBalancedRestClientBuilder
                .baseUrl("http://auth-service")
                .requestFactory(requestFactory)
                .build();
    }
}