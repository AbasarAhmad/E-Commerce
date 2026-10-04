package com.ecommerce.order.service;

import org.springframework.stereotype.Service;

import com.ecommerce.order.dto.ProductInternalResponse;
import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.dto.OrderItemRequest;

@Service
public class OrderInventoryService {

	private final ProductServiceClient productServiceClient;

	public OrderInventoryService(ProductServiceClient productServiceClient) {
		this.productServiceClient = productServiceClient;
	}

	public ProductInternalResponse getProduct(Long productId, String authorizationHeader) {

		return productServiceClient.getProductById(productId, authorizationHeader);
	}

	public void decrease(Long productId, Integer quantity, String authorizationHeader) {

		productServiceClient.decreaseProductQuantity(productId, quantity, authorizationHeader);
	}

	public void restore(Long productId, Integer quantity, String authorizationHeader) {

		productServiceClient.restoreProductQuantity(productId, quantity, authorizationHeader);
	}
}