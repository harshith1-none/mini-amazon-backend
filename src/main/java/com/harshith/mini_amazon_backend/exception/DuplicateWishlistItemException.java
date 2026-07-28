package com.harshith.mini_amazon_backend.exception;

public class DuplicateWishlistItemException extends RuntimeException {
    public DuplicateWishlistItemException(Long productId) {
        super("Product id " + productId + " is already in the wishlist");
    }
}
