package com.ecommerce.order.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.UserResponse;
import com.ecommerce.order.service.OrderService;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestParam Long userId, @Valid @RequestBody OrderRequest request) {

        OrderResponse response =orderService.createOrder(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    
    @GetMapping("/test-user/{username}")
    public ResponseEntity<UserResponse> testUser(@PathVariable String username,@RequestHeader("Authorization") String authorizationHeader) {

        return ResponseEntity.ok( orderService.getUser(username,authorizationHeader));
    }
}