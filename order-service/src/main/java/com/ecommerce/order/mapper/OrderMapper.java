package com.ecommerce.order.mapper;

import org.springframework.stereotype.Component;

import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.Order;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}