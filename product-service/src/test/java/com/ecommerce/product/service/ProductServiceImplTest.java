package com.ecommerce.product.service;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.exception.DuplicateSkuException;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.mapper.ProductMapper;
import com.ecommerce.product.repository.ProductRepository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.ecommerce.product.dto.ProductPageResponse;
import com.ecommerce.product.dto.ProductSearchRequest;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
    }
    
    @Test
    void shouldReturnProductWhenProductExists() {
        Long productId = 16L;
        Product product = new Product();
        product.setId(productId);
        product.setName("Oppo A5s");

        ProductResponse response = new ProductResponse();
        response.setId(productId);
        response.setName("Oppo A5s");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);
        ProductResponse result =productService.getProductById(productId);

        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Oppo A5s", result.getName());

        verify(productRepository).findById(productId);
        verify(productMapper).toResponse(product);
    }
    
    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        ProductNotFoundException exception =assertThrows(ProductNotFoundException.class,() -> productService.getProductById(productId));
        assertEquals("Product not found with id: 999",exception.getMessage());
        verify(productRepository).findById(productId);
        verify(productMapper, never()).toResponse(any(Product.class));
    }
    
    @Test
    void shouldCreateProductSuccessfully() {

        ProductRequest request = new ProductRequest();
        request.setName("iPhone 17");
        request.setSku("IPHONE-17");
        request.setPrice(new BigDecimal("99999.00"));
        request.setQuantity(10);
        request.setCategory("MOBILE");
        request.setStatus("ACTIVE");
        
        Product product = new Product();
        product.setId(20L);
        product.setName("iPhone 17");
        product.setSku("IPHONE-17");

        Product savedProduct = new Product();
        savedProduct.setId(20L);
        savedProduct.setName("iPhone 17");
        savedProduct.setSku("IPHONE-17");

        ProductResponse response = new ProductResponse();
        response.setId(20L);
        response.setName("iPhone 17");
        response.setSku("IPHONE-17");

        when(productRepository.existsBySku("IPHONE-17")).thenReturn(false);
        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(savedProduct);
        when(productMapper.toResponse(savedProduct)).thenReturn(response);
        ProductResponse result =productService.createProduct(request);

        assertNotNull(result);
        assertEquals(20L, result.getId());
        assertEquals("iPhone 17", result.getName());
        assertEquals("IPHONE-17", result.getSku());

        verify(productRepository).existsBySku("IPHONE-17");
        verify(productMapper).toEntity(request);
        verify(productRepository).save(product);
        verify(productMapper).toResponse(savedProduct);
    }
    
    @Test
    void shouldThrowExceptionWhenSkuAlreadyExists() {

        ProductRequest request = new ProductRequest();
        request.setName("iPhone 17");
        request.setSku("IPHONE-17");

        when(productRepository.existsBySku("IPHONE-17")).thenReturn(true);
        DuplicateSkuException exception =assertThrows( DuplicateSkuException.class,() -> productService.createProduct(request));
        assertEquals("Product with SKU 'IPHONE-17' already exists",exception.getMessage());

        verify(productRepository).existsBySku("IPHONE-17");
        verify(productRepository, never()).save(any(Product.class));
        verify(productMapper, never()).toEntity(any(ProductRequest.class));
    }
    
    @Test
    void shouldUpdateProductSuccessfully() {
        Long productId = 16L;
        
        ProductRequest request = new ProductRequest();
        request.setName("Oppo A5s Updated");
        request.setDescription("Updated description");
        request.setSku("OPPO-UPDATED");
        request.setPrice(new BigDecimal("1300000.00"));
        request.setQuantity(10);
        request.setCategory("MOBILE");
        request.setStatus("ACTIVE");

        Product product = new Product();
        product.setId(productId);
        product.setName("Oppo A5s");
        product.setDescription("Old description");
        product.setSku("OPPO");
        product.setPrice(new BigDecimal("1250000.00"));
        product.setQuantity(5);
        product.setCategory("MOBILE");
        product.setStatus("ACTIVE");

        ProductResponse response = new ProductResponse();
        response.setId(productId);
        response.setName("Oppo A5s Updated");
        response.setSku("OPPO-UPDATED");
        response.setPrice(new BigDecimal("1300000.00"));
        response.setQuantity(10);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);
        ProductResponse result =productService.updateProduct(productId, request);

        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Oppo A5s Updated", result.getName());
        assertEquals("OPPO-UPDATED", result.getSku());
        assertEquals(new BigDecimal("1300000.00"),result.getPrice());
        assertEquals(10, result.getQuantity());

        // Verify the entity was actually modified
        assertEquals("Oppo A5s Updated", product.getName());
        assertEquals("OPPO-UPDATED", product.getSku());
        assertEquals(new BigDecimal("1300000.00"),product.getPrice());
        assertEquals(10, product.getQuantity());

        verify(productRepository).findById(productId);
        verify(productMapper).toResponse(product);

        // Important: update method should NOT call save()
        verify(productRepository, never()).save(any(Product.class));
    }
    
    
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingProduct() {
        Long productId = 999L;

        ProductRequest request = new ProductRequest();
        request.setName("Updated Product");
        request.setSku("UPDATED-SKU");
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        ProductNotFoundException exception =
                assertThrows(ProductNotFoundException.class,() -> productService.updateProduct(productId,request));
        assertEquals("Product not found with id: 999",exception.getMessage());
        verify(productRepository).findById(productId);
        verify(productMapper, never()).toResponse(any(Product.class));
    }
    
    @Test
    void shouldDeleteProductSuccessfully() {
        Long productId = 16L;
        Product product = new Product();
        product.setId(productId);
        product.setName("Oppo A5s");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        productService.deleteProduct(productId);
        verify(productRepository).findById(productId);
        verify(productRepository).delete(product);
    }
    
    
    @Test
    void shouldThrowExceptionWhenDeletingNonExistingProduct() {
        Long productId = 999L;
        when(productRepository.findById(productId)) .thenReturn(Optional.empty());
        ProductNotFoundException exception =assertThrows(ProductNotFoundException.class,() -> productService.deleteProduct(productId));
        assertEquals("Product not found with id: 999",exception.getMessage());
        verify(productRepository).findById(productId);
        verify(productRepository, never()).delete(any(Product.class));
    }
    
    @Test
    void shouldSearchProductsSuccessfully() {

        ProductSearchRequest request = new ProductSearchRequest();
        request.setName("Oppo");
        request.setCategory("MOBILE");

        Pageable pageable =PageRequest.of(0, 10);
        Product product = new Product();
        product.setId(16L);
        product.setName("Oppo A5s");

        ProductResponse response = new ProductResponse();
        response.setId(16L);
        response.setName("Oppo A5s");
        Page<Product> productPage =new PageImpl<>(List.of(product),pageable,1);

        when(productRepository.findAll(any(Specification.class),eq(pageable))).thenReturn(productPage);
        when(productMapper.toResponse(product)).thenReturn(response);
        
        ProductPageResponse result =productService.searchProducts(request,pageable);

        assertNotNull(result);

        assertEquals(1, result.getContent().size());
        assertEquals(16L, result.getContent().get(0).getId());
        assertEquals("Oppo A5s",result.getContent().get(0).getName());

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());

        verify(productRepository).findAll(any(Specification.class),eq(pageable));
        verify(productMapper).toResponse(product);
    }
}