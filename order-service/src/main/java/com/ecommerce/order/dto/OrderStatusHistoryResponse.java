package com.ecommerce.order.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderStatusHistoryResponse {

    private String status;
    private LocalDateTime changedAt;
}