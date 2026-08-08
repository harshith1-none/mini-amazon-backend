package com.harshith.mini_amazon_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponseDto {

    private Long id;
    private Long productId;

    // userId + userName rather than a nested "user" object - same reasoning
    // as WishlistResponseDto/OrderItemResponseDto flattening product info
    // instead of nesting a full ProductResponseDto: the frontend needs "who
    // wrote this" for display, not the reviewer's email, role, or password
    // hash, so returning the full User entity (or even a full UserDto)
    // would both over-expose data and force extra frontend unwrapping.
    private Long userId;
    private String userName;

    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}