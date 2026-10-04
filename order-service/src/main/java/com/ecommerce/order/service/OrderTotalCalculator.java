package com.ecommerce.order.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ecommerce.order.entity.OrderItem;

@Component
public class OrderTotalCalculator {

    public BigDecimal calculate(List<OrderItem> items) {

        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}