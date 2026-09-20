package com.ecommerce.order.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.ecommerce.order.dto.UserResponse;

@Component
public class AuthServiceClient {

    private final RestClient restClient;

    public AuthServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public UserResponse getUserByUsername(String username,String authorizationHeader) {

        return restClient
                .get()
                .uri("/api/v1/auth/internal/users/{username}", username)
                .header("Authorization", authorizationHeader)
                .retrieve()
                .body(UserResponse.class);
    }
}