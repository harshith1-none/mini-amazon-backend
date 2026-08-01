package com.harshith.mini_amazon_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// No OrderRequestDto: placing an order takes nothing from the client - it's
// fully derived from the logged-in user's cart server-side (POST with an
// empty body). Adding an empty request DTO "just to have one" would be
// dead code with no fields to validate.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {
    private Long id;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal totalAmount;
    private List<OrderItemResponseDto> items;
}
