package com.ecommerce.product.repository;

import com.ecommerce.product.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
	boolean existsBySku(String sku);
	Page<Product> findByNameContainingIgnoreCase(String name,Pageable pageable);

}