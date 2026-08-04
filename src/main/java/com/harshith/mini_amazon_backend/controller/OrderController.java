package com.harshith.mini_amazon_backend.controller;

import com.harshith.mini_amazon_backend.dto.OrderResponseDto;
import com.harshith.mini_amazon_backend.dto.OrderStatusUpdateRequestDto;
import com.harshith.mini_amazon_backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // POST /api/orders, no request body - not /api/orders/place or
    // /checkout. The order is fully computed server-side from the logged-in
    // user's cart, so "create a new order" on the orders collection is all
    // this endpoint needs to mean. No RPC-style verb in the URL.
    @PostMapping
    public ResponseEntity<OrderResponseDto> placeOrder() {
        OrderResponseDto order = orderService.placeOrder();
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getOrders() {
        return ResponseEntity.ok(orderService.getOrders());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }


    //Day 10
    // PATCH, not PUT - the client is changing one field (status) on the
    // order, not replacing the whole resource. Admin-only: enforced in
    // SecurityConfig (HttpMethod.PATCH, "/api/orders/*/status" ->
    // hasRole("ADMIN")), not with an @PreAuthorize annotation here, to stay
    // consistent with how every other authorization rule in this project is
    // already declared centrally in SecurityConfig.
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequestDto request) {
        OrderResponseDto updatedOrder = orderService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok(updatedOrder);
    }
}
