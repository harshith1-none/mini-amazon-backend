package com.harshith.mini_amazon_backend.entity;

/**
 * Day 10: full order lifecycle. PLACED is the only state an order can be
 * created in (see OrderServiceImpl.placeOrder); every other value is only
 * ever reached through the admin status-update endpoint, and only via a
 * transition allowed by OrderServiceImpl.VALID_TRANSITIONS.
 */
public enum OrderStatus {
    PLACED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}