package com.harshith.mini_amazon_backend.exception;

import com.harshith.mini_amazon_backend.entity.OrderStatus;

public class InvalidOrderStatusTransitionException extends RuntimeException {
    public InvalidOrderStatusTransitionException(OrderStatus currentStatus, OrderStatus requestedStatus) {
        super("Cannot change order status from " + currentStatus + " to " + requestedStatus);
    }
}