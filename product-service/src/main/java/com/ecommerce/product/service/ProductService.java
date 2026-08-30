package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

	ProductResponse createProduct(ProductRequest request);
	 ProductResponse getProductById(Long id);
	 Page<ProductResponse> getAllProducts(Pageable pageable);
	 ProductResponse updateProduct(Long id, ProductRequest request);
	 void deleteProduct(Long id);
	 Page<ProductResponse> searchProducts(String name,Pageable pageable);
}
