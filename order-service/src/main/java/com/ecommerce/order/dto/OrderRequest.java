package com.ecommerce.order.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {

	@NotEmpty(message = "Order must contain at least one item")
	@Valid
	private List<OrderItemRequest> items;
}