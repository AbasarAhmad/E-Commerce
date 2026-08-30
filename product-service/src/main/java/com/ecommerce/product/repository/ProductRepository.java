package com.ecommerce.product.repository;

import com.ecommerce.product.entity.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Long>,JpaSpecificationExecutor<Product> {
	boolean existsBySku(String sku);
	Page<Product> findByNameContainingIgnoreCase(String name,Pageable pageable);
	

}
//JpaSpecificationExecutor : It gives our repository the ability to execute dynamic specifications.