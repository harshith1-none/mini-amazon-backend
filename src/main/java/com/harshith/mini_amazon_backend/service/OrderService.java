package com.harshith.mini_amazon_backend.service;

import com.harshith.mini_amazon_backend.dto.OrderResponseDto;

import java.util.List;

public interface OrderService {

    OrderResponseDto placeOrder();

    List<OrderResponseDto> getOrders();

    OrderResponseDto getOrderById(Long orderId);
}
