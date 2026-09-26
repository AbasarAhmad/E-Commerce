package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RestoreProductQuantityRequest {

    private Integer quantity;
}