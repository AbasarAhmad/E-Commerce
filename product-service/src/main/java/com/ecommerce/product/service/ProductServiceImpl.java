package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductPageResponse;
import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.dto.ProductSearchRequest;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.exception.DuplicateSkuException;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.mapper.ProductMapper;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.specification.ProductSpecification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(
            ProductRepository productRepository,
            ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

//    @Override
//    public ProductResponse createProduct(ProductRequest request) {
//        Product product = productMapper.toEntity(request);
//        Product savedProduct = productRepository.save(product);
//        return productMapper.toResponse(savedProduct);
//    }

    
    @Override
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateSkuException("Product with SKU '" + request.getSku() + "' already exists");
        }
        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }
    
    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->new ProductNotFoundException("Product not found with id: " + id));
        return productMapper.toResponse(product);
    }
    
//    @Override
//    public Page<ProductResponse> getAllProducts(Pageable pageable) {
//        return productRepository.findAll(pageable)
//                .map(productMapper::toResponse);
//    }
    
    @Override
    public ProductPageResponse getAllProducts(Pageable pageable) {

        Page<Product> productPage =productRepository.findAll(pageable);

        return ProductPageResponse.builder()
                .content(
                        productPage.getContent()
                                .stream()
                                .map(productMapper::toResponse)
                                .toList()
                )
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .build();
    }
    
    
    
    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->new ProductNotFoundException("Product not found with id: " + id));
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(request.getCategory());
        product.setStatus(request.getStatus());
        Product updatedProduct = productRepository.save(product);
        return productMapper.toResponse(updatedProduct);
    }
    
    
    
    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->new ProductNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }
    
    
    
    @Override
    public Page<ProductResponse> searchProducts(String name,Pageable pageable) {

        return productRepository
                .findByNameContainingIgnoreCase(name, pageable)
                .map(productMapper::toResponse);
    }
    
    
//    @Override
//    public Page<ProductResponse> searchProducts(ProductSearchRequest searchRequest,Pageable pageable) {
//        Specification<Product> specification =
//                ProductSpecification.filterProducts(
//                        searchRequest.getName(),
//                        searchRequest.getCategory(),
//                        searchRequest.getStatus(),
//                        searchRequest.getMinPrice(),
//                        searchRequest.getMaxPrice()
//                );
//
//        return productRepository
//                .findAll(specification, pageable)
//                .map(productMapper::toResponse);
//    }
    
    @Override
    public ProductPageResponse searchProducts(ProductSearchRequest searchRequest,Pageable pageable) {
        Specification<Product> specification =
                ProductSpecification.filterProducts(
                        searchRequest.getName(),
                        searchRequest.getCategory(),
                        searchRequest.getStatus(),
                        searchRequest.getMinPrice(),
                        searchRequest.getMaxPrice()
                );
        Page<Product> productPage =productRepository.findAll(specification, pageable);

        return ProductPageResponse.builder()
                .content(
                        productPage.getContent()
                                .stream()
                                .map(productMapper::toResponse)
                                .toList()
                )
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .first(productPage.isFirst())
                .last(productPage.isLast())
                .build();
    }
}