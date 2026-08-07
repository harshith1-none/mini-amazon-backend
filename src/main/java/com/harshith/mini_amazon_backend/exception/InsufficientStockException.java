package com.harshith.mini_amazon_backend.exception;

public class InsufficientStockException extends RuntimeException {
    // UPDATED (Day 13): reworded to lead with "Only X available", matching
    // the response text the Day 13 spec asks for ("Only 3 items available
    // in stock"), while still naming the product so existing tests that
    // assert on the product name (see OrderServiceTest) keep passing.
    public InsufficientStockException(String productName, int available, int requested) {
        super("Only " + available + " item(s) of '" + productName
                + "' available in stock (requested " + requested + ").");
    }
}