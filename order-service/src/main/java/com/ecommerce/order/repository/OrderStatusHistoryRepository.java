package com.ecommerce.order.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.order.entity.OrderStatusHistory;

public interface OrderStatusHistoryRepository
        extends JpaRepository<OrderStatusHistory, Long> {
	Page<OrderStatusHistory> findByOrderIdOrderByChangedAtAsc(
	        Long orderId,
	        Pageable pageable);
}