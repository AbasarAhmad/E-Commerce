package com.ecommerce.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
	@Schema(description = "Unique product identifier", example = "1")
    private Long id;
	
	@Schema(description = "Product name", example = "iPhone 17")
    private String name;
	
	@Schema(description = "Unique product SKU", example = "IPHONE-17-001")
    private String description;
	
	 @Schema(description = "Unique product SKU (Stock Keeping Unit)",example = "IPHONE-17-001")
    private String sku;
	 
	 @Schema(description = "Brand or manufacturer of the product",example = "Apple")
    private String brand;
	 
	 @Schema(description = "Product price",example = "79999.99")
    private BigDecimal price;
	 
	 @Schema(description = "Quantity of the product",example = "50" )
    private Integer quantity;
	 
	 @Schema(description = "Product category",example = "MOBILE")
    private String category;
	 
	 @Schema(description = "Current status of the product",example = "ACTIVE")
    private String status;
	 
	 @Schema(description = "Date and time when the product was created",example = "2026-09-10T10:30:00")
    private LocalDateTime createdAt;
	 
	 @Schema(description = "Date and time when the product was last updated",example = "2026-09-10T12:45:00")
    private LocalDateTime updatedAt;
}