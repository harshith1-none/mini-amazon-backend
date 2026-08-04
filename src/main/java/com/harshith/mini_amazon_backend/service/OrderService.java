package com.harshith.mini_amazon_backend.service;

import com.harshith.mini_amazon_backend.dto.OrderResponseDto;
import com.harshith.mini_amazon_backend.entity.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponseDto placeOrder();

    List<OrderResponseDto> getOrders();

    OrderResponseDto getOrderById(Long orderId);



    // NEW (Day 10): admin-only. Not scoped to the caller's own orders -
    // applies to any order in the system.
    OrderResponseDto updateOrderStatus(Long orderId, OrderStatus newStatus);
}
