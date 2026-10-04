package com.ecommerce.order.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.persistence.OptimisticLockException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AuthServiceException.class)
	public ResponseEntity<ErrorResponse> handleAuthServiceException(AuthServiceException ex) {

		ErrorResponse error = new ErrorResponse(LocalDateTime.now(), ex.getStatus().value(),
				ex.getStatus().getReasonPhrase(), ex.getMessage());

		return ResponseEntity.status(ex.getStatus()).body(error);
	}

	@ExceptionHandler(InsufficientProductQuantityException.class)
	public ResponseEntity<ErrorResponse> handleInsufficientProductQuantity(InsufficientProductQuantityException ex) {

		ErrorResponse error = new ErrorResponse(LocalDateTime.now(), HttpStatus.CONFLICT.value(),
				HttpStatus.CONFLICT.getReasonPhrase(), ex.getMessage());

		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

	@ExceptionHandler(OrderNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {

		ErrorResponse error = new ErrorResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(),
				HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage());

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(InvalidOrderStatusException.class)
	public ResponseEntity<ErrorResponse> handleInvalidOrderStatus(InvalidOrderStatusException ex) {

		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
				"Bad Request", ex.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(OrderCancellationException.class)
	public ResponseEntity<ErrorResponse> handleOrderCancellation(OrderCancellationException ex) {

		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_GATEWAY.value(),
				"Bad Gateway", ex.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
	}

	@ExceptionHandler(InvalidOrderRequestException.class)
	public ResponseEntity<ErrorResponse> handleInvalidOrderRequest(InvalidOrderRequestException ex) {

		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
				"Bad Request", ex.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {

		Map<String, Object> response = new HashMap<>();

		response.put("status", 403);
		response.put("message", ex.getMessage());

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
	}

	@ExceptionHandler(OptimisticLockException.class)
	public ResponseEntity<ErrorResponse> handleOptimisticLockException(OptimisticLockException ex) {

		ErrorResponse error = new ErrorResponse(LocalDateTime.now(), HttpStatus.CONFLICT.value(), "Conflict",
				"Order was modified by another request. Please retry.");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

//	@ExceptionHandler(DataIntegrityViolationException.class)
//	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
//		ErrorResponse error = new ErrorResponse(LocalDateTime.now(), HttpStatus.CONFLICT.value(), "Conflict",
//				"Request conflicts with existing data");
//		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
//	}

	@ExceptionHandler(DuplicateIdempotencyKeyException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateIdempotencyKey(DuplicateIdempotencyKeyException ex) {

		ErrorResponse error = new ErrorResponse(LocalDateTime.now(), HttpStatus.CONFLICT.value(), "Conflict",
				ex.getMessage());

		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}
}