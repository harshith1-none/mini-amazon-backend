package com.harshith.mini_amazon_backend.exception;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(Long productId) {
        super("Cart item not found for product id: " + productId);
    }
}