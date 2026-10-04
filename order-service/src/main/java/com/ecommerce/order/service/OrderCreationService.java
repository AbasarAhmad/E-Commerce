package com.ecommerce.order.service;

import org.springframework.stereotype.Component;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderStatus;

@Component
public class OrderCreationService {

	public Order create(Long userId) {

		return Order.builder().userId(userId).status(OrderStatus.CREATED).build();
	}
}