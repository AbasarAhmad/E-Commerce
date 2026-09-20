package com.ecommerce.order.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.order.client.AuthServiceClient;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.UserResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final AuthServiceClient authServiceClient;

    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper,AuthServiceClient authServiceClient) {

        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.authServiceClient = authServiceClient;
    }

    @Transactional
    public OrderResponse createOrder(
            String username,
            String authorizationHeader,
            OrderRequest request) {

        UserResponse userResponse =
                authServiceClient.getUserByUsername(
                        username,
                        authorizationHeader);

        LocalDateTime now = LocalDateTime.now();

        Order order = Order.builder()
                .userId(userResponse.getId())
                .totalAmount(request.getTotalAmount())
                .status("CREATED")
                .createdAt(now)
                .updatedAt(now)
                .build();

        Order savedOrder = orderRepository.save(order);

        return orderMapper.toResponse(savedOrder);
    }
}