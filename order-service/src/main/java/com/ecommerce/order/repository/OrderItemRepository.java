package com.ecommerce.order.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.order.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
//	List<OrderItem> findByOrderId(Long orderId);
	Page<OrderItem> findByOrderId(Long orderId, Pageable pageable);
}