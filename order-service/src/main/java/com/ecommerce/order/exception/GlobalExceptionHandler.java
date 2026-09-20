package com.ecommerce.order.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AuthServiceException.class)
	public ResponseEntity<ErrorResponse> handleAuthServiceException(
	        AuthServiceException ex) {

	    ErrorResponse error = new ErrorResponse(
	            LocalDateTime.now(),
	            ex.getStatus().value(),
	            ex.getStatus().getReasonPhrase(),
	            ex.getMessage()
	    );

	    return ResponseEntity
	            .status(ex.getStatus())
	            .body(error);
	}
}