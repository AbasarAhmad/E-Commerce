package com.ecommerce.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

	@Schema(description = "Name of the product", example = "iPhone 17")
    @NotBlank(message = "Product name is required")
    private String name;

	@Schema(description = "Detailed description of the product", example = "Latest Apple smartphone")
    private String description;
    
    @Schema(description = "Unique product SKU (Stock Keeping Unit)",example = "IPHONE-17-001")
    @NotBlank(message = "Stock Keeping Unit")
    private String sku;

    @Schema(description = "Brand of the product",example = "Apple")
    private String brand;
    
    @Schema(description = "Product price",example = "79999.00")
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @Schema(description = "Available product quantity",example = "10")
    @NotNull(message = "Quantity is required")
    @PositiveOrZero(message = "Quantity cannot be negative")
    private Integer quantity;

    @Schema(description = "Product category",example = "Electronics")
    @NotBlank(message = "Category is required")
    private String category;

    @Schema(description = "Current product status",example = "ACTIVE")
    @NotBlank(message = "Status is required")
    private String status;
}