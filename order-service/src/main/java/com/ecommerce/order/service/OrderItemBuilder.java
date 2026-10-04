package com.ecommerce.order.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.ecommerce.order.dto.OrderItemRequest;
import com.ecommerce.order.dto.ProductInternalResponse;
import com.ecommerce.order.entity.OrderItem;

@Component
public class OrderItemBuilder {

	public OrderItem build(OrderItemRequest request, ProductInternalResponse product) {

		BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

		return OrderItem.builder().productId(product.getId()).quantity(request.getQuantity())
				.unitPrice(product.getPrice()).subtotal(subtotal).build();
	}
}