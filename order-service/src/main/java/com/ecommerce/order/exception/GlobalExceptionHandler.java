package com.ecommerce.order.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}