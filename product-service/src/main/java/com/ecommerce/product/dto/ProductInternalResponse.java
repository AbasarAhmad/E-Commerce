package com.ecommerce.product.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductInternalResponse {

    private Long id;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String status;
}