package com.ecommerce.order.entity;

import com.ecommerce.order.exception.InvalidOrderStatusException;

public enum OrderStatus {

	CREATED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED;

	public boolean canTransitionTo(OrderStatus newStatus) {

		return switch (this) {
		case CREATED -> newStatus == CONFIRMED || newStatus == CANCELLED;

		case CONFIRMED -> newStatus == SHIPPED || newStatus == CANCELLED;

		case SHIPPED -> newStatus == DELIVERED || newStatus == CANCELLED;

		case DELIVERED, CANCELLED -> false;
		};
	}

	public boolean canBeCancelled() {
		return this == CREATED || this == CONFIRMED || this == SHIPPED;
	}

	public void validateTransitionTo(OrderStatus newStatus) {
		if (!canTransitionTo(newStatus)) {
			throw new InvalidOrderStatusException("Order cannot transition from " + this + " to " + newStatus);
		}
	}
}