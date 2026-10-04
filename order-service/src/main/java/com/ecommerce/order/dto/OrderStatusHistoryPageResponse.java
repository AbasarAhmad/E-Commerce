package com.ecommerce.order.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderStatusHistoryPageResponse {

    private List<OrderStatusHistoryResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}