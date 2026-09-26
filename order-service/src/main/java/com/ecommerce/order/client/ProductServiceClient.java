package com.ecommerce.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.ecommerce.order.dto.ProductInternalResponse;
import com.ecommerce.order.dto.RestoreProductQuantityRequest;
import com.ecommerce.order.dto.UpdateProductQuantityRequest;

@Component
public class ProductServiceClient {

    private final RestClient restClient;

    @Value("${services.product.url}")
    private String productServiceUrl;

    public ProductServiceClient(
            @LoadBalanced RestClient.Builder loadBalancedRestClientBuilder) {

        this.restClient = loadBalancedRestClientBuilder.build();
    }

    public ProductInternalResponse getProductById(
            Long productId,
            String authorizationHeader) {

        return restClient
                .get()
                .uri(
                    productServiceUrl
                        + "/api/v1/products/internal/{productId}",
                    productId
                )
                .header("Authorization", authorizationHeader)
                .retrieve()
                .body(ProductInternalResponse.class);
    }
    public void decreaseProductQuantity(
            Long productId,
            Integer quantity,
            String authorizationHeader) {

            restClient
                .patch()
                .uri( productServiceUrl+ "/api/v1/products/internal/{productId}/quantity", productId)
                .header("Authorization", authorizationHeader)
                .body(new UpdateProductQuantityRequest(quantity))
                .retrieve()
                .toBodilessEntity();
    }
    
    public void restoreProductQuantity(
            Long productId,
            Integer quantity,
            String authorizationHeader) {

        restClient
                .patch()
                .uri(
                    productServiceUrl
                        + "/api/v1/products/internal/{productId}/quantity/restore",
                    productId
                )
                .header("Authorization", authorizationHeader)
                .body(
                    new RestoreProductQuantityRequest(quantity)
                )
                .retrieve()
                .toBodilessEntity();
    }
}