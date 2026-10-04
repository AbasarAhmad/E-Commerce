package com.ecommerce.order.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.ecommerce.order.dto.OrderItemRequest;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.exception.InvalidOrderRequestException;

@Component
public class OrderRequestValidator {

	private static final int MAX_ITEMS = 20;
	private static final int MAX_TOTAL_QUANTITY = 500;

	public void validate(OrderRequest request) {
		// for do not add product size more than 20
		if (request.getItems().size() > MAX_ITEMS) {
			throw new InvalidOrderRequestException("An order cannot contain more than " + MAX_ITEMS + " items");
		}

		int totalQuantity = request.getItems().stream().mapToInt(OrderItemRequest::getQuantity).sum();
		// total quantity limit of 500 units per order.
		if (totalQuantity > MAX_TOTAL_QUANTITY) {
			throw new InvalidOrderRequestException("Total quantity in an order cannot exceed " + MAX_TOTAL_QUANTITY);
		}

		Set<Long> productIds = new HashSet<>();
		// For not add Duplicate Item products in a single request
		for (OrderItemRequest item : request.getItems()) {

			if (!productIds.add(item.getProductId())) {
				throw new InvalidOrderRequestException("Duplicate product in order: " + item.getProductId());
			}
		}
	}
}