package com.ecommerce.order.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.OptimisticLockException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(AuthServiceException.class)
	public ResponseEntity<ErrorResponse> handleAuthServiceException(AuthServiceException ex) {

		log.warn("Auth Service request failed. status={}, message={}", ex.getStatus().value(), ex.getMessage());

		ErrorResponse error = new ErrorResponse(LocalDateTime.now(), ex.getStatus().value(),
				ex.getStatus().getReasonPhrase(), ex.getMessage());

		return ResponseEntity.status(ex.getStatus()).body(error);
	}

	@ExceptionHandler(InsufficientProductQuantityException.class)
	public ResponseEntity<ErrorResponse> handleInsufficientProductQuantity(InsufficientProductQuantityException ex) {

		log.warn("Insufficient product quantity: {}", ex.getMessage());

		ErrorResponse error = new ErrorResponse(LocalDateTime.now(), HttpStatus.CONFLICT.value(),
				HttpStatus.CONFLICT.getReasonPhrase(), ex.getMessage());

		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

	@ExceptionHandler(OrderNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {

		log.warn("Order not found: {}", ex.getMessage());

		ErrorResponse error = new ErrorResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(),
				HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage());

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(InvalidOrderStatusException.class)
	public ResponseEntity<ErrorResponse> handleInvalidOrderStatus(InvalidOrderStatusException ex) {

		log.warn("Invalid order status request: {}", ex.getMessage());

		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(), ex.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(OrderCancellationException.class)
	public ResponseEntity<ErrorResponse> handleOrderCancellation(OrderCancellationException ex) {

		log.error("Order cancellation failed: {}", ex.getMessage(), ex);

		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_GATEWAY.value(),
				HttpStatus.BAD_GATEWAY.getReasonPhrase(), ex.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
	}

	@ExceptionHandler(InvalidOrderRequestException.class)
	public ResponseEntity<ErrorResponse> handleInvalidOrderRequest(InvalidOrderRequestException ex) {

		log.warn("Invalid order request: {}", ex.getMessage());

		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(), ex.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {

		log.warn("Access denied: {}", ex.getMessage());

		Map<String, Object> response = new HashMap<>();
		response.put("status", HttpStatus.FORBIDDEN.value());
		response.put("message", ex.getMessage());

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
	}

	@ExceptionHandler(OptimisticLockException.class)
	public ResponseEntity<ErrorResponse> handleOptimisticLockException(OptimisticLockException ex) {

		/*
		 * Optimistic locking prevents two concurrent requests from silently overwriting
		 * each other's order changes.
		 */
		log.warn("Optimistic locking conflict while updating order.");

		ErrorResponse error = new ErrorResponse(LocalDateTime.now(), HttpStatus.CONFLICT.value(),
				HttpStatus.CONFLICT.getReasonPhrase(), "Order was modified by another request. Please retry.");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

	@ExceptionHandler(DuplicateIdempotencyKeyException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateIdempotencyKey(DuplicateIdempotencyKeyException ex) {

		log.warn("Duplicate idempotency key detected: {}", ex.getMessage());

		ErrorResponse error = new ErrorResponse(LocalDateTime.now(), HttpStatus.CONFLICT.value(),
				HttpStatus.CONFLICT.getReasonPhrase(), ex.getMessage());

		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

		log.error("Unhandled exception: type={} message={}", ex.getClass().getSimpleName(), ex.getMessage(), ex);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponse(LocalDateTime.now(),HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error",ex.getMessage()));
	}


}
