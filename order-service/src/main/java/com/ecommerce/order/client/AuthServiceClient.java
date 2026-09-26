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

@Component
public class AuthServiceClient {

	private final RestClient restClient;

	@Value("${services.auth.url}")
	private String authServiceUrl;

	public AuthServiceClient(RestClient authServiceRestClient) {
		this.restClient = authServiceRestClient;
	}

	public UserResponse getUserByUsername(String username, String authorizationHeader) {
		try {
			return restClient.get().uri(authServiceUrl + "/api/v1/auth/internal/users/{username}", username)
					.header("Authorization", authorizationHeader).retrieve().body(UserResponse.class);
		} catch (HttpClientErrorException.Unauthorized e) {

			throw new AuthServiceException("Authentication failed while calling Auth Service", HttpStatus.UNAUTHORIZED,
					e);

		} catch (HttpClientErrorException.NotFound e) {
			throw new AuthServiceException("User not found in Auth Service", HttpStatus.NOT_FOUND, e);
		} catch (HttpClientErrorException e) {
			throw new AuthServiceException("Auth Service rejected the request: " + e.getStatusCode(),
					HttpStatus.BAD_GATEWAY, e);

		} catch (HttpServerErrorException e) {
			throw new AuthServiceException("Auth Service returned a server error: " + e.getStatusCode(),
					HttpStatus.BAD_GATEWAY, e);

		} catch (ResourceAccessException e) {
			throw new AuthServiceException("Unable to connect to Auth Service", HttpStatus.BAD_GATEWAY, e);
		}
	}
}