package com.ecommerce.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.order.client.AuthServiceClient;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.UserResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest orderRequest;
    private UserResponse userResponse;
    private Order savedOrder;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {

        orderRequest = new OrderRequest();
        orderRequest.setTotalAmount(new BigDecimal("1499.00"));

        userResponse = new UserResponse(
                6L,
                "admin",
                "ADMIN"
        );

        savedOrder = Order.builder()
                .id(8L)
                .userId(6L)
                .totalAmount(new BigDecimal("1499.00"))
                .status("CREATED")
                .build();

        orderResponse = new OrderResponse(
                8L,
                6L,
                new BigDecimal("1499.00"),
                "CREATED",
                null,
                null
        );
    }

    @Test
    void createOrder_shouldCreateOrderForAuthenticatedUser() {

        String username = "admin";
        String authorizationHeader = "Bearer test-jwt";

        when(authServiceClient.getUserByUsername(
                username,
                authorizationHeader))
                .thenReturn(userResponse);

        when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class)))
                .thenReturn(savedOrder);

        when(orderMapper.toResponse(savedOrder))
                .thenReturn(orderResponse);

        OrderResponse response = orderService.createOrder(
                username,
                authorizationHeader,
                orderRequest
        );

        assertNotNull(response);
        assertEquals(8L, response.getId());
        assertEquals(6L, response.getUserId());
        assertEquals(
                new BigDecimal("1499.00"),
                response.getTotalAmount()
        );
        assertEquals("CREATED", response.getStatus());

        verify(authServiceClient)
                .getUserByUsername(username, authorizationHeader);

        verify(orderRepository)
                .save(org.mockito.ArgumentMatchers.any(Order.class));
    }
}