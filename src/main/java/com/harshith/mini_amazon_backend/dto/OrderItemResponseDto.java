package com.harshith.mini_amazon_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponseDto {
    private Long productId;
    private String productName;
    private String productImageUrl;
    private BigDecimal price;
    private int quantity;
    private BigDecimal lineTotal;
}
