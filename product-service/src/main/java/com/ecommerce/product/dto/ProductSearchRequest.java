package com.ecommerce.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductSearchRequest {

    private String name;
    private String category;
    private String status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}