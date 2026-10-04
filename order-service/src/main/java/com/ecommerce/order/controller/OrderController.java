package com.ecommerce.order.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.ecommerce.order.dto.OrderCreationResult;
import com.ecommerce.order.dto.OrderItemResponse;
import com.ecommerce.order.dto.OrderPageResponse;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.OrderStatusHistoryPageResponse;
import com.ecommerce.order.dto.OrderStatusResponse;
import com.ecommerce.order.dto.UpdateOrderStatusRequest;
import com.ecommerce.order.mapper.PaginationConstants;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.order.service.PaginationValidator;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

	private static final Logger log = LoggerFactory.getLogger(OrderController.class);

	private final OrderService orderService;
	private final PaginationValidator paginationValidator;

	public OrderController(OrderService orderService, PaginationValidator paginationValidator) {
		this.orderService = orderService;
		this.paginationValidator = paginationValidator;
	}

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(Authentication authentication,
			@RequestHeader("Authorization") String authorizationHeader, @Valid @RequestBody OrderRequest request,
			@RequestHeader("Idempotency-Key") String idempotencyKey) {

		String username = authentication.getName();

		log.info("Creating order for user. username={}, idempotencyKey={}", username, idempotencyKey);

		OrderCreationResult response = orderService.createOrder(username, authorizationHeader, request, idempotencyKey);

		/*
		 * A repeated request with the same idempotency key returns the existing order
		 * instead of creating a duplicate order.
		 */
		if (response.isCreated()) {
			return ResponseEntity.status(HttpStatus.CREATED).header("Idempotency-Key", idempotencyKey)
					.body(response.getOrder());
		}

		return ResponseEntity.ok().header("Idempotency-Key", idempotencyKey).body(response.getOrder());
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId, Authentication authentication,
			@RequestHeader("Authorization") String authorizationHeader) {

		String username = authentication.getName();

		boolean isAdmin = isAdmin(authentication);

		OrderResponse response = orderService.getOrderById(orderId, username, isAdmin, authorizationHeader);

		return ResponseEntity.ok(response);
	}

	@GetMapping
	public ResponseEntity<OrderPageResponse> getOrders(Authentication authentication,
			@RequestHeader("Authorization") String authorizationHeader, Pageable pageable) {
		log.info("Getting all order's ");
		paginationValidator.validate(pageable);
		paginationValidator.validateOrderSorting(pageable);

		pageable = paginationValidator.applyDefaultSort(pageable, PaginationConstants.DEFAULT_ORDER_SORT_FIELD,
				PaginationConstants.DEFAULT_ORDER_SORT_DIRECTION);

		String username = authentication.getName();

		String role = authentication.getAuthorities().stream().findFirst().map(authority -> authority.getAuthority())
				.orElse("");

		OrderPageResponse response = orderService.getOrders(username, role, authorizationHeader, pageable);

		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{orderId}/status")
	public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long orderId, Authentication authentication,
			@RequestHeader("Authorization") String authorizationHeader,
			@Valid @RequestBody UpdateOrderStatusRequest request) {

		log.info("Getting order's Status");
		boolean isAdmin = isAdmin(authentication);

		log.info("Updating order status. orderId={}, status={}, isAdmin={}", orderId, request.getStatus(), isAdmin);

		OrderResponse response = orderService.updateOrderStatus(orderId, request.getStatus(), isAdmin,
				authorizationHeader);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{orderId}/status")
	public ResponseEntity<OrderStatusResponse> getOrderStatus(@PathVariable Long orderId, Authentication authentication,
			@RequestHeader("Authorization") String authorizationHeader) {

		String username = authentication.getName();
		boolean isAdmin = isAdmin(authentication);

		OrderStatusResponse response = orderService.getOrderStatus(orderId, username, isAdmin, authorizationHeader);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/{orderId}/cancel")
	public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId, Authentication authentication,
			@RequestHeader("Authorization") String authorizationHeader) {

		String username = authentication.getName();
		boolean isAdmin = isAdmin(authentication);

		log.info("Cancelling order. orderId={}, username={}, isAdmin={}", orderId, username, isAdmin);

		OrderResponse response = orderService.cancelOrder(orderId, username, isAdmin, authorizationHeader);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{orderId}/history")
	public ResponseEntity<OrderStatusHistoryPageResponse> getOrderStatusHistory(@PathVariable Long orderId,
			Authentication authentication, @RequestHeader("Authorization") String authorizationHeader,
			Pageable pageable) {

		paginationValidator.validate(pageable);
		paginationValidator.validateOrderSorting(pageable);

		pageable = paginationValidator.applyDefaultSort(pageable, PaginationConstants.DEFAULT_HISTORY_SORT_FIELD,
				PaginationConstants.DEFAULT_HISTORY_SORT_DIRECTION);

		String username = authentication.getName();
		boolean isAdmin = isAdmin(authentication);

		OrderStatusHistoryPageResponse response = orderService.getOrderStatusHistory(orderId, username, isAdmin,
				authorizationHeader, pageable);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{orderId}/items")
	public ResponseEntity<Page<OrderItemResponse>> getOrderItems(@PathVariable Long orderId,
			Authentication authentication, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
			Pageable pageable) {

		paginationValidator.validate(pageable);
		paginationValidator.validateOrderSorting(pageable);

		pageable = paginationValidator.applyDefaultSort(pageable, PaginationConstants.DEFAULT_ORDER_ITEM_SORT_FIELD,
				PaginationConstants.DEFAULT_ORDER_ITEM_SORT_DIRECTION);

		String username = authentication.getName();
		boolean isAdmin = isAdmin(authentication);

		return ResponseEntity.ok(orderService.getOrderItems(orderId, username, isAdmin, authorizationHeader, pageable));
	}

	/**
	 * Checks whether the authenticated user has the ADMIN role. Keeping this check
	 * in one place avoids repeating the same stream logic across multiple
	 * controller methods.
	 */
	private boolean isAdmin(Authentication authentication) {
		return authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
	}
}