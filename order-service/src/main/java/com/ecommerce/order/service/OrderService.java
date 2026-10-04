package com.ecommerce.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.order.client.AuthServiceClient;
import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.OrderStatusHistoryPageResponse;
import com.ecommerce.order.dto.OrderStatusHistoryResponse;
import com.ecommerce.order.dto.OrderStatusResponse;
import com.ecommerce.order.dto.ProductInternalResponse;
import com.ecommerce.order.dto.UserResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.entity.OrderStatusHistory;
import com.ecommerce.order.exception.DuplicateIdempotencyKeyException;
import com.ecommerce.order.exception.InsufficientProductQuantityException;
import com.ecommerce.order.exception.InvalidOrderRequestException;
import com.ecommerce.order.exception.InvalidOrderStatusException;
import com.ecommerce.order.exception.OrderCancellationException;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.OrderStatusHistoryRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import com.ecommerce.order.dto.UserResponse;
import com.ecommerce.order.dto.OrderCreationResult;
import com.ecommerce.order.dto.OrderItemRequest;
import com.ecommerce.order.dto.OrderItemResponse;
import com.ecommerce.order.dto.OrderPageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderMapper orderMapper;
	private final AuthServiceClient authServiceClient;
	private final ProductServiceClient productServiceClient;
	private final OrderStatusHistoryRepository orderStatusHistoryRepository;
	private final OrderTotalCalculator orderTotalCalculator;
	private final OrderRequestValidator orderRequestValidator;
	private final OrderItemBuilder orderItemBuilder;
	private final OrderInventoryService orderInventoryService;
	private final OrderCreationService orderCreationService;
	private final OrderHistoryService orderHistoryService;
	private final OrderItemRepository orderItemRepository;
	private static final BigDecimal MAX_ORDER_AMOUNT = new BigDecimal("1000000.00");
	private static final int MAX_IDEMPOTENCY_KEY_LENGTH = 100;
	private static final Pattern IDEMPOTENCY_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

	public OrderService(OrderRepository orderRepository, OrderMapper orderMapper, AuthServiceClient authServiceClient,
			ProductServiceClient productServiceClient, OrderStatusHistoryRepository orderStatusHistoryRepository,
			OrderTotalCalculator orderTotalCalculator, OrderRequestValidator orderRequestValidator,
			OrderItemBuilder orderItemBuilder, OrderInventoryService orderInventoryService,
			OrderCreationService orderCreationService, OrderHistoryService orderHistoryService,
			OrderItemRepository orderItemRepository) {

		this.orderRepository = orderRepository;
		this.orderMapper = orderMapper;
		this.authServiceClient = authServiceClient;
		this.productServiceClient = productServiceClient;
		this.orderStatusHistoryRepository = orderStatusHistoryRepository;
		this.orderTotalCalculator = orderTotalCalculator;
		this.orderRequestValidator = orderRequestValidator;
		this.orderItemBuilder = orderItemBuilder;
		this.orderInventoryService = orderInventoryService;
		this.orderCreationService = orderCreationService;
		this.orderHistoryService = orderHistoryService;
		this.orderItemRepository = orderItemRepository;
	}

	private static final Logger log = LoggerFactory.getLogger(OrderService.class);

	@Transactional
	public OrderCreationResult createOrder(String username, String authorizationHeader, OrderRequest request,
			String idempotencyKey) {

		if (idempotencyKey == null || idempotencyKey.isBlank()) {
			throw new InvalidOrderRequestException("Idempotency-Key header is required");
		}

		if (idempotencyKey.length() > MAX_IDEMPOTENCY_KEY_LENGTH) {
			throw new InvalidOrderRequestException(
					"Idempotency-Key cannot exceed " + MAX_IDEMPOTENCY_KEY_LENGTH + " characters");
		}
		if (!IDEMPOTENCY_KEY_PATTERN.matcher(idempotencyKey).matches()) {
			throw new InvalidOrderRequestException("Idempotency-Key contains invalid characters");
		}

		UserResponse userResponse = authServiceClient.getUserByUsername(username, authorizationHeader);
		Long userId = userResponse.getId();
		Optional<Order> existingOrder = orderRepository.findByIdempotencyKeyAndUserId(idempotencyKey, userId);

		if (existingOrder.isPresent()) {
			return new OrderCreationResult(orderMapper.toResponse(existingOrder.get()), false);
		}

		orderRequestValidator.validate(request);

		Order order = orderCreationService.create(userResponse.getId());
		order.setIdempotencyKey(idempotencyKey);
		List<ProductInternalResponse> products = new ArrayList<>();

		try {

			for (OrderItemRequest itemRequest : request.getItems()) {

				ProductInternalResponse product = orderInventoryService.getProduct(itemRequest.getProductId(),
						authorizationHeader);

				if (product.getQuantity() < itemRequest.getQuantity()) {
					throw new InsufficientProductQuantityException(
							"Insufficient quantity for product: " + product.getId());
				}

				OrderItem orderItem = orderItemBuilder.build(itemRequest, product);

				orderItem.setOrder(order);

				order.getItems().add(orderItem);

				orderInventoryService.decrease(product.getId(), itemRequest.getQuantity(), authorizationHeader);

				products.add(product);
			}

			order.setTotalAmount(orderTotalCalculator.calculate(order.getItems()));

			if (order.getTotalAmount().compareTo(MAX_ORDER_AMOUNT) > 0) {
				throw new InvalidOrderRequestException("Order total cannot exceed ₹1,000,000");
			}

			Order savedOrder;

			try {
				savedOrder = orderRepository.save(order);
			} catch (DataIntegrityViolationException ex) {

				if (isIdempotencyKeyViolation(ex)) {
					Order existingOrder1 = getExistingOrderByIdempotencyKey(idempotencyKey);

					return new OrderCreationResult(orderMapper.toResponse(existingOrder1), true);
				}
				throw ex;
			}

			orderHistoryService.record(savedOrder, OrderStatus.CREATED);

			return new OrderCreationResult(orderMapper.toResponse(savedOrder), true);

		} catch (RuntimeException e) {

			for (int i = 0; i < products.size(); i++) {

				ProductInternalResponse product = products.get(i);

				OrderItemRequest itemRequest = request.getItems().get(i);

				try {
					orderInventoryService.restore(product.getId(), itemRequest.getQuantity(), authorizationHeader);

				} catch (RuntimeException compensationException) {

					log.error("Failed to restore product {} quantity {} " + "during order creation compensation",
							product.getId(), itemRequest.getQuantity(), compensationException);
				}
			}

			throw e;
		}
	}

	@Transactional(readOnly = true)
	public OrderResponse getOrderById(Long orderId, String username, boolean isAdmin, String authorizationHeader) {

		UserResponse userResponse = authServiceClient.getUserByUsername(username, authorizationHeader);

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

		validateOrderAccess(order, userResponse.getId(), isAdmin);

		return orderMapper.toResponse(order);
	}

	@Transactional(readOnly = true)
	public OrderPageResponse getOrders(String username, String role, String authorizationHeader, Pageable pageable) {

		Page<Order> orders;

		if ("ROLE_ADMIN".equals(role)) {

			orders = orderRepository.findAll(pageable);

		} else {

			UserResponse userResponse = authServiceClient.getUserByUsername(username, authorizationHeader);

			orders = orderRepository.findByUserId(userResponse.getId(), pageable);
		}

		Page<OrderResponse> responsePage = orders.map(orderMapper::toResponse);

		return new OrderPageResponse(responsePage.getContent(), responsePage.getNumber(), responsePage.getSize(),
				responsePage.getTotalElements(), responsePage.getTotalPages());
	}

	@Transactional
	public OrderResponse updateOrderStatus(Long orderId, String status, Boolean isAdmin, String authorizationHeader) {

		if (!isAdmin) {
			throw new AccessDeniedException("Only admin can update order status");
		}
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

		OrderStatus currentStatus = order.getStatus();

		OrderStatus newStatus = validateStatusTransition(currentStatus, status);

		if (newStatus == OrderStatus.CANCELLED) {
			restoreOrderItems(order, authorizationHeader);
		}

		order.setStatus(newStatus);
		Order updatedOrder = orderRepository.save(order);

		orderHistoryService.record(updatedOrder, newStatus);

		return orderMapper.toResponse(updatedOrder);
	}

	private void validateOrderAccess(Order order, Long userId, boolean isAdmin) {

		if (isAdmin) {
			return;
		}

		if (!order.getUserId().equals(userId)) {
			throw new OrderNotFoundException("Order not found with id: " + order.getId());
		}
	}

	private OrderStatus validateStatusTransition(OrderStatus currentStatus, String requestedStatus) {

		final OrderStatus newStatus;

		try {
			newStatus = OrderStatus.valueOf(requestedStatus.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new InvalidOrderStatusException("Invalid order status: " + requestedStatus);
		}

		currentStatus.validateTransitionTo(newStatus);

		return newStatus;
	}

	private void restoreOrderItems(Order order, String authorizationHeader) {

		List<OrderItem> restoredItems = new ArrayList<>();

		try {

			log.info("Cancelling order {} with {} items", order.getId(), order.getItems().size());

			for (OrderItem item : order.getItems()) {

				log.info("Restoring product {} quantity {} for order {}", item.getProductId(), item.getQuantity(),
						order.getId());

				orderInventoryService.restore(item.getProductId(), item.getQuantity(), authorizationHeader);

				restoredItems.add(item);
			}

		} catch (RuntimeException e) {

			log.error("Inventory restoration failed for order {}. Starting compensation.", order.getId(), e);

			for (OrderItem item : restoredItems) {

				try {
					orderInventoryService.decrease(item.getProductId(), item.getQuantity(), authorizationHeader);

				} catch (RuntimeException compensationException) {

					log.error("Compensation failed for product {} quantity {}", item.getProductId(), item.getQuantity(),
							compensationException);
				}
			}

			throw new OrderCancellationException("Unable to cancel order because product stock restoration failed", e);
		}
	}

	@Transactional(readOnly = true)
	public OrderStatusResponse getOrderStatus(Long orderId, String username, boolean isAdmin,
			String authorizationHeader) {
		UserResponse userResponse = authServiceClient.getUserByUsername(username, authorizationHeader);

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

		validateOrderAccess(order, userResponse.getId(), isAdmin);

//		return new OrderStatusResponse(order.getId(), order.getStatus().name());
		return orderMapper.toStatusResponse(order);
	}

	@Transactional
	public OrderResponse cancelOrder(Long orderId, String username, boolean isAdmin, String authorizationHeader) {

		UserResponse userResponse = authServiceClient.getUserByUsername(username, authorizationHeader);

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

		validateOrderAccess(order, userResponse.getId(), isAdmin);

		if (!order.getStatus().canBeCancelled()) {
			throw new InvalidOrderStatusException("Order with status " + order.getStatus() + " cannot be cancelled");
		}

		restoreOrderItems(order, authorizationHeader);

		order.setStatus(OrderStatus.CANCELLED);

		Order updatedOrder = orderRepository.save(order);
		orderHistoryService.record(updatedOrder, OrderStatus.CANCELLED);
		return orderMapper.toResponse(updatedOrder);
	}

	@Transactional(readOnly = true)
	public OrderStatusHistoryPageResponse getOrderStatusHistory(Long orderId, String username, Boolean isAdmin,
			String authorizationHeader, Pageable pageable) {
		if (pageable.getPageNumber() < 0) {
			throw new InvalidOrderRequestException("Page number cannot be negative");
		}
		UserResponse userResponse = authServiceClient.getUserByUsername(username, authorizationHeader);

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

		validateOrderAccess(order, userResponse.getId(), isAdmin);

//		Page<OrderStatusHistory> historyPage = orderStatusHistoryRepository.findByOrderIdOrderByChangedAtAsc(orderId,
//				pageable);
		Page<OrderStatusHistoryResponse> responsePage = orderHistoryService.getHistory(orderId, pageable);
//		Page<OrderStatusHistoryResponse> responsePage = historyPage.map(orderMapper::toStatusHistoryResponse);

//		return new OrderStatusHistoryPageResponse(responsePage.getContent(), responsePage.getNumber(),
//				responsePage.getSize(), responsePage.getTotalElements(), responsePage.getTotalPages());
		return new OrderStatusHistoryPageResponse(responsePage.getContent(), responsePage.getNumber(),
				responsePage.getSize(), responsePage.getTotalElements(), responsePage.getTotalPages());
	}

	@Transactional(readOnly = true)
	public Page<OrderItemResponse> getOrderItems(Long orderId, String username, boolean isAdmin,
			String authorizationHeader, Pageable pageable) {

		Long userId = authServiceClient.getUserByUsername(username, authorizationHeader).getId();

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

		validateOrderAccess(order, userId, isAdmin);

		Page<OrderItem> itemPage = orderItemRepository.findByOrderId(orderId, pageable);

		return itemPage.map(orderMapper::toItemResponse);
	}

	private boolean isIdempotencyKeyViolation(DataIntegrityViolationException ex) {
		Throwable cause = ex;

		while (cause != null) {
			if (cause.getMessage() != null && cause.getMessage().contains("uk_orders_idempotency_key")) {
				return true;
			}

			cause = cause.getCause();
		}

		return false;
	}

	private Order getExistingOrderByIdempotencyKey(String idempotencyKey) {

		return orderRepository.findByIdempotencyKey(idempotencyKey)
				.orElseThrow(() -> new DuplicateIdempotencyKeyException("Idempotency-Key has already been used"));
	}
}