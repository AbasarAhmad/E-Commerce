package com.ecommerce.order.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
	Page<Order> findByUserId(Long userId, Pageable pageable);

	Optional<Order> findByIdempotencyKey(String idempotencyKey);

	Optional<Order> findByIdempotencyKeyAndUserId(String idempotencyKey, Long userId);
}