package com.ecommerce.product;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService;
//
//@SpringBootTest
//@ActiveProfiles("test")
//class ProductServiceIntegrationTest {
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    @Test
//    void shouldSaveAndFindProduct() {
//
//        Product product = Product.builder()
//                .name("iPhone 17")
//                .description("Apple smartphone")
//                .sku("IPHONE-17-001")
//                .price(new BigDecimal("79999.00"))
//                .quantity(10)
//                .category("Electronics")
//                .status("ACTIVE")
//                .build();
//
//        // Save
//        Product savedProduct = productRepository.save(product);
//
//        // Find
//        Product foundProduct = productRepository.findById(savedProduct.getId())
//                .orElseThrow();
//
//        // Verify
//        assertNotNull(savedProduct.getId());
//        assertEquals("iPhone 17", foundProduct.getName());
//        assertEquals("IPHONE-17-001", foundProduct.getSku());
//        assertEquals(new BigDecimal("79999.00"), foundProduct.getPrice());
//        assertEquals(10, foundProduct.getQuantity());
//    }
//}



@SpringBootTest
@ActiveProfiles("test")
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldCreateProductSuccessfully() {

        ProductRequest request = new ProductRequest();

        request.setName("Samsung Galaxy S26");
        request.setDescription("Samsung smartphone");
        request.setSku("SAMSUNG-S26-001");
        request.setPrice(new BigDecimal("69999.00"));
        request.setQuantity(20);
        request.setCategory("Electronics");
        request.setStatus("ACTIVE");

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response.getId());
        assertEquals("Samsung Galaxy S26", response.getName());
        assertEquals("SAMSUNG-S26-001", response.getSku());

        Product savedProduct = productRepository
                .findById(response.getId())
                .orElseThrow();

        assertEquals("Samsung Galaxy S26", savedProduct.getName());
        assertEquals("SAMSUNG-S26-001", savedProduct.getSku());
        assertEquals(new BigDecimal("69999.00"), savedProduct.getPrice());
        assertEquals(20, savedProduct.getQuantity());
    }
}