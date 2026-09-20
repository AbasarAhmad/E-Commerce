package com.ecommerce.order.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.OrderService;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

//    @PostMapping
//    public ResponseEntity<OrderResponse> createOrder(@RequestParam Long userId, @Valid @RequestBody OrderRequest request) {
//
//        OrderResponse response =orderService.createOrder(userId, request);
//
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(response);
//    }
//    
    
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(Authentication authentication,@RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody OrderRequest request) {

        String username = authentication.getName();

        OrderResponse response =orderService.createOrder(username,authorizationHeader,request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}