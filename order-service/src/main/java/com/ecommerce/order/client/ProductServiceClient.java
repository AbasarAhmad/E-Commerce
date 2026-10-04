package com.ecommerce.order.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.ecommerce.order.dto.ProductInternalResponse;
import com.ecommerce.order.dto.RestoreProductQuantityRequest;
import com.ecommerce.order.dto.UpdateProductQuantityRequest;

@Component
public class ProductServiceClient {

	private static final Logger log = LoggerFactory.getLogger(ProductServiceClient.class);

	private final RestClient restClient;

	@Value("${services.product.url}")
	private String productServiceUrl;

	public ProductServiceClient(@LoadBalanced RestClient.Builder loadBalancedRestClientBuilder) {
		this.restClient = loadBalancedRestClientBuilder.build();
	}

	public ProductInternalResponse getProductById(Long productId, String authorizationHeader) {
		try {
			log.info("Fetching product details from Product Service. productId={}", productId);

			return restClient.get()
					.uri(productServiceUrl + "/api/v1/products/internal/{productId}", productId)
					.header("Authorization", authorizationHeader)
					.header("X-Correlation-Id", MDC.get("correlationId"))
					.retrieve()
					.body(ProductInternalResponse.class);

		} catch (RuntimeException e) {
			log.error("Failed to fetch product from Product Service. productId={}", productId, e);
			throw e;
		}
	}

	public void decreaseProductQuantity(Long productId, Integer quantity, String authorizationHeader) {
		try {
			log.info("Decreasing product quantity. productId={}, quantity={}", productId, quantity);

			/*
			 * Quantity is updated through Product Service instead of directly
			 * modifying the product database from Order Service.
			 */
			restClient.patch()
					.uri(productServiceUrl + "/api/v1/products/internal/{productId}/quantity", productId)
					.header("Authorization", authorizationHeader)
					.body(new UpdateProductQuantityRequest(quantity))
					.retrieve()
					.toBodilessEntity();

		} catch (RuntimeException e) {
			log.error("Failed to decrease product quantity. productId={}, quantity={}",
					productId, quantity, e);
			throw e;
		}
	}

	public void restoreProductQuantity(Long productId, Integer quantity, String authorizationHeader) {
		try {
			log.debug("Restoring product quantity. productId={}, quantity={}", productId, quantity);

			/*
			 * Used when a previously reserved quantity needs to be returned,
			 * for example when an order is cancelled or processing fails.
			 */
			restClient.patch()
					.uri(productServiceUrl + "/api/v1/products/internal/{productId}/quantity/restore", productId)
					.header("Authorization", authorizationHeader)
					.body(new RestoreProductQuantityRequest(quantity))
					.retrieve()
					.toBodilessEntity();

		} catch (RuntimeException e) {
			log.error("Failed to restore product quantity. productId={}, quantity={}",
					productId, quantity, e);
			throw e;
		}
	}
}
