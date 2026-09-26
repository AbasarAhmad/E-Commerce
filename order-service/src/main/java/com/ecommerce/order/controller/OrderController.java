package com.ecommerce.order.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

//    @PostMapping
//    public ResponseEntity<OrderResponse> createOrder(@RequestParam Long userId, @Valid @RequestBody OrderRequest request) {
//
//        OrderResponse response =orderService.createOrder(userId, request);
//
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(response);
//    }
//    

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(Authentication authentication,
			@RequestHeader("Authorization") String authorizationHeader, @Valid @RequestBody OrderRequest request) {

		String username = authentication.getName();

		OrderResponse response = orderService.createOrder(username, authorizationHeader, request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId, Authentication authentication,
			@RequestHeader("Authorization") String authorizationHeader) {

		String username = authentication.getName();

		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

		OrderResponse response = orderService.getOrderById(orderId, username, isAdmin, authorizationHeader);

		return ResponseEntity.ok(response);
	}

	@GetMapping
	public ResponseEntity<Page<OrderResponse>> getOrders(Authentication authentication,
			@RequestHeader("Authorization") String authorizationHeader, Pageable pageable) {

		String username = authentication.getName();

		String role = authentication.getAuthorities().stream().findFirst().map(authority -> authority.getAuthority())
				.orElse("");

		Page<OrderResponse> response = orderService.getOrders(username, role, authorizationHeader, pageable);

		return ResponseEntity.ok(response);
	}
}