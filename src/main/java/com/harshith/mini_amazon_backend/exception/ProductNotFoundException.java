package com.harshith.mini_amazon_backend.exception;

public class ProductNotFoundException extends RuntimeException{
    public ProductNotFoundException(Long id) {
        super("Product not found with id : " + id);
    }
}
