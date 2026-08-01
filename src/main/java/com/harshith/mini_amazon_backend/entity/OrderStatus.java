package com.harshith.mini_amazon_backend.entity;

/**
 * Minimal for Part 1 - order creation only. Future days (Part 2) will add
 * transitions like CONFIRMED, SHIPPED, DELIVERED, CANCELLED and the
 * endpoints to move between them. Not adding those states now since
 * nothing in today's task touches order status changes.
 */
public enum OrderStatus {
    PLACED
}
