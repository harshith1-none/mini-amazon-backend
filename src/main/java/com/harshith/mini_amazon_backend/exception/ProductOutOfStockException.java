package com.harshith.mini_amazon_backend.exception;

// Distinct from InsufficientStockException on purpose: this is the
// "there is nothing left at all" case (stock == 0), which reads better as
// a plain "Product is out of stock." than as "0 available, requested 1".
// InsufficientStockException stays for the "some stock exists, but not
// enough for what you asked" case.
public class ProductOutOfStockException extends RuntimeException {
    public ProductOutOfStockException(String productName) {
        super("Product '" + productName + "' is out of stock.");
    }
}
