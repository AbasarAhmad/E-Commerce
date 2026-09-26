package com.ecommerce.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.order.client.AuthServiceClient;
import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.ProductInternalResponse;
import com.ecommerce.order.dto.UserResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.exception.InsufficientProductQuantityException;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecommerce.order.dto.UserResponse;

@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderMapper orderMapper;
	private final AuthServiceClient authServiceClient;
	private final ProductServiceClient productServiceClient;

	public OrderService(OrderRepository orderRepository, OrderMapper orderMapper, AuthServiceClient authServiceClient,
			ProductServiceClient productServiceClient) {

		this.orderRepository = orderRepository;
		this.orderMapper = orderMapper;
		this.authServiceClient = authServiceClient;
		this.productServiceClient = productServiceClient;
	}

	@Transactional
	public OrderResponse createOrder(String username, String authorizationHeader, OrderRequest request) {

		// 1. Get authenticated user
		UserResponse userResponse = authServiceClient.getUserByUsername(username, authorizationHeader);

		// 2. Get product from Product Service
		ProductInternalResponse product = productServiceClient.getProductById(request.getProductId(),
				authorizationHeader);

		// 3. Check product availability
		if (product.getQuantity() < request.getQuantity()) {
			throw new InsufficientProductQuantityException(
					"Insufficient product quantity for product: " + product.getId());
		}
		productServiceClient.decreaseProductQuantity(request.getProductId(), request.getQuantity(),
				authorizationHeader);

		try {
			// 4. Create order
			LocalDateTime now = LocalDateTime.now();
			BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

			OrderItem orderItem = OrderItem.builder().productId(product.getId()).quantity(request.getQuantity())
					.unitPrice(product.getPrice()).subtotal(totalAmount).build();

			Order order = Order.builder().userId(userResponse.getId()).totalAmount(totalAmount).status("CREATED")
					.createdAt(now).updatedAt(now).build();
			orderItem.setOrder(order);
			order.getItems().add(orderItem);

			Order savedOrder = orderRepository.save(order);
			return orderMapper.toResponse(savedOrder);
		} catch (RuntimeException ex) {
			productServiceClient.restoreProductQuantity(request.getProductId(), request.getQuantity(),
					authorizationHeader);

			throw ex;
		}
	}

	public OrderResponse getOrderById(Long orderId, String username, boolean isAdmin, String authorizationHeader) {

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
		if (!isAdmin) {

			UserResponse userResponse = authServiceClient.getUserByUsername(username, authorizationHeader);

			if (!order.getUserId().equals(userResponse.getId())) {
				throw new OrderNotFoundException("Order not found with id: " + orderId);
			}
		}
		return orderMapper.toResponse(order);
	}

	public Page<OrderResponse> getOrders(String username, String role, String authorizationHeader, Pageable pageable) {

		Page<Order> orders;

		if ("ROLE_ADMIN".equals(role)) {

			orders = orderRepository.findAll(pageable);

		} else {

			UserResponse userResponse = authServiceClient.getUserByUsername(username, authorizationHeader);

			orders = orderRepository.findByUserId(userResponse.getId(), pageable);
		}

		return orders.map(orderMapper::toResponse);
	}
}