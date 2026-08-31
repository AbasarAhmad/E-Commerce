package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductPageResponse;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.dto.ProductSearchRequest;
import com.ecommerce.product.service.ProductService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }
    
    
    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {

        return productService.getProductById(id);
    }
    
    
//    http://localhost:8081/api/v1/products?page=1&size=5&sort=price,desc
//    @GetMapping
//    public Page<ProductResponse> getAllProducts(Pageable pageable) {
//
//        return productService.getAllProducts(pageable);
//    }
//    
    
    @GetMapping
    public ProductPageResponse getAllProducts(Pageable pageable) {

        return productService.getAllProducts(pageable);
    }
    
    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable Long id,@Valid @RequestBody ProductRequest request) {
        return productService.updateProduct(id, request);
    }
    
    
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
    
    
//    http://localhost:8081/api/v1/products/search?name=iphone   =======>  For Search
//    http://localhost:8081/api/v1/products/search?name=iphone&page=0&size=2  ======>  For pagination
//    http://localhost:8081/api/v1/products/search?name=iphone&page=0&size=5&sort=price,desc    ===> for Sorting
    @GetMapping("/search")
    public Page<ProductResponse> searchProducts(@RequestParam String name,Pageable pageable) {
        return productService.searchProducts(name, pageable);
    }
    
    
//    http://localhost:8081/api/v1/products/search?category=MOBILE  => Category
//    http://localhost:8081/api/v1/products/search?category=MOBILE&status=ACTIVE  => Category + Test
//    http://localhost:8081/api/v1/products/search?minPrice=50000&maxPrice=200000  => Price Range
//    http://localhost:8081/api/v1/products/search?name=iphone&category=MOBILE&status=ACTIVE&minPrice=50000&maxPrice=300000&page=0&size=5&sort=price,desc => All Filter
//    @GetMapping("/advance/search")
//    public Page<ProductResponse> searchProducts(ProductSearchRequest searchRequest,Pageable pageable) {
//        return productService.searchProducts(searchRequest,pageable);
//    }
    
    
    @GetMapping("/advance/search")
    public ProductPageResponse searchProducts(ProductSearchRequest searchRequest,Pageable pageable) {
        return productService.searchProducts(searchRequest,pageable);
    }
}