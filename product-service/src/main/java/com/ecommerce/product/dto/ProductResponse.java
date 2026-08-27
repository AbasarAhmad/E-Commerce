package com.ecommerce.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private Integer quantity;
    private String category;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // getters and setters
}