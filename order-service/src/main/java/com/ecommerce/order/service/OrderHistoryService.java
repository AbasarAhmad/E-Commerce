package com.ecommerce.order.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.order.dto.OrderStatusHistoryResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.entity.OrderStatusHistory;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.repository.OrderStatusHistoryRepository;

@Service
public class OrderHistoryService {

	private final OrderStatusHistoryRepository repository;
	private final OrderMapper orderMapper;

	public OrderHistoryService(OrderStatusHistoryRepository repository, OrderMapper orderMapper) {
		this.repository = repository;
		this.orderMapper = orderMapper;
	}

	public void record(Order order, OrderStatus status) {

		OrderStatusHistory history = OrderStatusHistory.builder().order(order).status(status).build();

		repository.save(history);
	}

	@Transactional(readOnly = true)
	public Page<OrderStatusHistoryResponse> getHistory(Long orderId, Pageable pageable) {

		Page<OrderStatusHistory> historyPage = repository.findByOrderIdOrderByChangedAtAsc(orderId, pageable);

		return historyPage.map(orderMapper::toStatusHistoryResponse);
	}
}