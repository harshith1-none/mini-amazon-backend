package com.harshith.mini_amazon_backend.exception;

public class WishlistItemNotFoundException extends RuntimeException {
    public WishlistItemNotFoundException(Long productId) {
        super("Wishlist item not found for product id: " + productId);
    }
}