package com.harshith.mini_amazon_backend.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String category) {
        super("No products found in category: " + category);
    }
}