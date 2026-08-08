package com.harshith.mini_amazon_backend.exception;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(Long reviewId) {
        super("Review not found with id: " + reviewId);
    }
}
