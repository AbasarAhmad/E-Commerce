package com.ecommerce.order.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.ecommerce.order.dto.UserResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.ecommerce.order.exception.AuthServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Component
public class AuthServiceClient {

	private static final Logger log = LoggerFactory.getLogger(AuthServiceClient.class);

	private final RestClient restClient;

	@Value("${services.auth.url}")
	private String authServiceUrl;

	public AuthServiceClient(RestClient authServiceRestClient) {
		this.restClient = authServiceRestClient;
	}

	public UserResponse getUserByUsername(String username, String authorizationHeader) {
        try {
            /*
             * Forward the correlation ID so the request can be traced across
             * order-service and auth-service using the same identifier.
             */
            String correlationId = MDC.get("correlationId");

            log.info("Fetching user details from Auth Service for username: {}", username);

            return restClient.get()
                    .uri(authServiceUrl + "/api/v1/auth/internal/users/{username}", username)
                    .header("Authorization", authorizationHeader)
                    .header("X-Correlation-Id", correlationId)
                    .retrieve()
                    .body(UserResponse.class);

        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Auth Service rejected user request due to unauthorized access. username={}", username);

            throw new AuthServiceException(
                    "Authentication failed while calling Auth Service",
                    HttpStatus.UNAUTHORIZED,
                    e
            );

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("User not found in Auth Service. username={}", username);

            throw new AuthServiceException(
                    "User not found in Auth Service",
                    HttpStatus.NOT_FOUND,
                    e
            );

        } catch (HttpClientErrorException e) {
            log.warn("Auth Service rejected the request. status={}, username={}",
                    e.getStatusCode(), username);

            throw new AuthServiceException(
                    "Auth Service rejected the request: " + e.getStatusCode(),
                    HttpStatus.BAD_GATEWAY,
                    e
            );

        } catch (HttpServerErrorException e) {
            log.error("Auth Service returned a server error. status={}, username={}",
                    e.getStatusCode(), username);

            throw new AuthServiceException(
                    "Auth Service returned a server error: " + e.getStatusCode(),
                    HttpStatus.BAD_GATEWAY,
                    e
            );

        } catch (ResourceAccessException e) {
            log.error("Unable to connect to Auth Service. username={}", username, e);

            throw new AuthServiceException(
                    "Unable to connect to Auth Service",
                    HttpStatus.BAD_GATEWAY,
                    e
            );
        }
    }
}