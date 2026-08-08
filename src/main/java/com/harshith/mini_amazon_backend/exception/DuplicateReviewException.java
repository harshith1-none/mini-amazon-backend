package com.harshith.mini_amazon_backend.exception;

public class DuplicateReviewException extends RuntimeException {
    public DuplicateReviewException(Long productId) {
        super("You have already reviewed product id " + productId);
    }
}