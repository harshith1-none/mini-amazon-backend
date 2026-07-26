package com.harshith.mini_amazon_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body for POST /api/cart (add an item) and PUT /api/cart/{productId}
 * (set quantity). productId is required for POST (identifies which
 * product to add) and, for PUT, must match the path's productId - see
 * CartController.updateQuantity for that consistency check.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartRequestDto {

    @NotNull(message = "Product id must not be null")
    private Long productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity = 1;
}