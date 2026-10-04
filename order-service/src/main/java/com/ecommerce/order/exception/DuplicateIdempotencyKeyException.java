package com.ecommerce.order.exception;

public class DuplicateIdempotencyKeyException extends RuntimeException {

    public DuplicateIdempotencyKeyException(String message) {
        super(message);
    }
}