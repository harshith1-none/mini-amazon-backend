package com.harshith.mini_amazon_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Never expose the Cart or Product entities directly - this flattens what
 * the frontend actually needs (product display info + line total) without
 * leaking JPA-managed entities or lazy-loading proxies into the JSON response.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponseDto {

    private Long id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private BigDecimal price;
    private int quantity;
    private BigDecimal lineTotal;
}