package com.ecommerce.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateProductSuccessfully() throws Exception {

        String requestBody = """
                {
                    "name": "MacBook Pro",
                    "description": "Apple laptop",
                    "sku": "MACBOOK-PRO-001",
                    "price": 149999.00,
                    "quantity": 10,
                    "category": "Electronics",
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("MacBook Pro"))
                .andExpect(jsonPath("$.sku").value("MACBOOK-PRO-001"))
                .andExpect(jsonPath("$.price").value(149999.00))
                .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    void shouldReturnBadRequestWhenProductRequestIsInvalid() throws Exception {

        String requestBody = """
                {
                    "name": "",
                    "sku": "",
                    "price": 0,
                    "quantity": -5,
                    "category": "",
                    "status": ""
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.sku").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.quantity").exists())
                .andExpect(jsonPath("$.errors.category").exists())
                .andExpect(jsonPath("$.errors.status").exists());
    }
    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {

        mockMvc.perform(get("/api/v1/products/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Product not found with id: 999999"));
    }
    
    @Test
    void shouldReturnConflictWhenSkuAlreadyExists() throws Exception {

        String requestBody = """
                {
                    "name": "MacBook Air",
                    "description": "Apple laptop",
                    "sku": "MACBOOK-DUPLICATE-001",
                    "price": 99999.00,
                    "quantity": 5,
                    "category": "Electronics",
                    "status": "ACTIVE"
                }
                """;

        // First request → product should be created
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());

        // Second request with same SKU → should fail
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Product with SKU 'MACBOOK-DUPLICATE-001' already exists"));
    }
    
    @Test
    void shouldGetAllProductsWithPagination() throws Exception {

        String product1 = """
                {
                    "name": "Laptop",
                    "description": "Business laptop",
                    "sku": "LAPTOP-PAGE-001",
                    "price": 80000.00,
                    "quantity": 10,
                    "category": "Electronics",
                    "status": "ACTIVE"
                }
                """;

        String product2 = """
                {
                    "name": "Monitor",
                    "description": "4K monitor",
                    "sku": "MONITOR-PAGE-001",
                    "price": 30000.00,
                    "quantity": 5,
                    "category": "Electronics",
                    "status": "ACTIVE"
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(product1))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(product2))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/products")
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }
}
