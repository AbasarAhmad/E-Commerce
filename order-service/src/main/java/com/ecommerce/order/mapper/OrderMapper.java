package com.ecommerce.order.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ecommerce.order.dto.OrderItemResponse;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.dto.OrderStatusHistoryResponse;
import com.ecommerce.order.dto.OrderStatusResponse;
import com.ecommerce.order.entity.OrderStatusHistory;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {

        List<OrderItemResponse> items =order.getItems()
                        .stream()
                        .map(this::toItemResponse)
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items
        );
    }

    public OrderItemResponse toItemResponse(OrderItem item) {

    	return new OrderItemResponse(
    	        item.getId(),
    	        item.getProductId(),
    	        item.getQuantity(),
    	        item.getUnitPrice(),
    	        item.getSubtotal(),
    	        item.getCreatedAt());
    }
    public OrderStatusHistoryResponse toStatusHistoryResponse(
            OrderStatusHistory history) {

        return new OrderStatusHistoryResponse(
                history.getStatus().name(),
                history.getChangedAt());
    }
    public OrderStatusResponse toStatusResponse(Order order) {

        return new OrderStatusResponse(
                order.getId(),
                order.getStatus().name());
    }
}